package com.ptit.booking.service.Impl;

import com.ptit.booking.configuration.ZaloPayConfig;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.zaloPay.CreateOrderRequest;
import com.ptit.booking.dto.zaloPay.CreateOrderResponse;
import com.ptit.booking.dto.zaloPay.OrderStatusResponse;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.model.Payment;
import com.ptit.booking.repository.PaymentRepository;
import com.ptit.booking.service.ZaloPayService;
import com.ptit.booking.util.HMACUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloPayServiceImpl implements ZaloPayService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentRepository paymentRepository;
    private HMACUtil hmacUtil;

    @Override
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(CreateOrderRequest request) {
        try {
            String appTransId = generateAppTransId();
            long appTime = System.currentTimeMillis();

            // Lưu giao dịch vào database
            Payment transaction = new Payment();
            transaction.setAppTransId(appTransId);
            transaction.setBooking(null);
            transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
            transaction.setPaymentStatus("PENDING");
            paymentRepository.save(transaction);

            Map<String, String> params = buildOrderParams(request, appTransId, appTime);
            log.info("ZaloPay Request Params: {}", params);

            String response = sendRequest(ZaloPayConfig.CREATE_ORDER_URL, params);
            JSONObject result = new JSONObject(response);
            log.info("ZaloPay Response: {}", result.toString());

            int returnCode = result.getInt("return_code");
            String returnMessage = result.getString("return_message");
            String zpTransToken = result.optString("zp_trans_token", "");
            String orderUrl = result.optString("order_url", "");

            if (returnCode != 1) {
                log.warn("ZaloPay order creation failed. ReturnCode: {}, ReturnMessage: {}", returnCode, returnMessage);
                transaction.setPaymentStatus("FAILED");
                transaction.setMessage(returnMessage);
                paymentRepository.save(transaction);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                ApiResponse.<CreateOrderResponse>builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .message("ZaloPay error: " + returnMessage)
                                    .data(null)
                                    .build()
                        );
            }

            CreateOrderResponse orderResponse = CreateOrderResponse.builder()
                    .zpTransToken(zpTransToken)
                    .appTransId(appTransId)
                    .orderUrl(orderUrl)
                    .returnCode(returnCode)
                    .returnMessage(returnMessage)
                    .build();

            return ResponseEntity.ok(
                    ApiResponse.<CreateOrderResponse>builder()
                            .statusCode(HttpStatus.OK.value())
                            .message(orderResponse.getReturnMessage())
                            .data(orderResponse)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to create ZaloPay order for orderId: {}", request.getOrderId(), e);
            throw new AppException(ErrorCode.ZALOPAY_ORDER_CREATION_FAILED);
        }
    }

    @Override
    public Map<String, Object> handleCallback(String jsonStr) {
        JSONObject result = new JSONObject();
        try {
            JSONObject cbdata = new JSONObject(jsonStr);
            String dataStr = cbdata.getString("data");
            String reqMac = cbdata.getString("mac");
            System.out.println("<---------------Callback is called-------------->");
            String computedMac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256,ZaloPayConfig.KEY2, dataStr);
            if (!computedMac.equals(reqMac)) {
                log.warn("Invalid HMAC for callback: {}", dataStr);
                return Map.of("return_code", -1, "return_message", "Invalid MAC");
            }

            JSONObject dataJson = new JSONObject(dataStr);
            log.info("ZaloPay callback data: {}", dataJson);
            String appTransId = dataJson.getString("app_trans_id");
            int amount = dataJson.getInt("amount");
            String zpTransId = dataJson.getString("zp_trans_id");

            log.info("Callback processed for app_trans_id: {}, zp_trans_id: {}", appTransId, zpTransId);
            return Map.of("return_code", 1, "return_message", "Success");
        } catch (Exception e) {
            log.error("Failed to process callback", e);
            return Map.of("return_code", -1, "return_message", "Callback processing failed");
        }
    }

    @Override
    public ResponseEntity<?> getOrderStatus(String appTransId) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("app_id", ZaloPayConfig.APP_ID);
            params.put("app_trans_id", appTransId);
            params.put("app_time", String.valueOf(System.currentTimeMillis()));

            String data = params.get("app_id") + "|" + params.get("app_trans_id") + "|" + ZaloPayConfig.KEY1;
            params.put("mac", HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256,ZaloPayConfig.KEY1, data));

            String response = sendRequest(ZaloPayConfig.GET_STATUS_PAY_URL, params);
            JSONObject result = new JSONObject(response);
            log.info("ZaloPay Response Status Payment: {}", result.toString());
            int returnCode = result.getInt("return_code");
            String returnMessage = result.getString("return_message");
            boolean isProcessing = result.getBoolean("is_processing");
            long amount = result.getLong("amount");
            String zpTransId = result.optString("zp_trans_id", "");

            OrderStatusResponse orderStatus = OrderStatusResponse.builder()
                    .returnCode(returnCode)
                    .returnMessage(returnMessage)
                    .isProcessing(isProcessing)
                    .amount(amount)
                    .zpTransId(zpTransId)
                    .build();

            // Xử lý từng trạng thái
            switch (returnCode) {
                case 1:
                    return ResponseEntity.ok(
                            ApiResponse.builder()
                                    .statusCode(HttpStatus.OK.value())
                                    .message("Thanh toán thành công")
                                    .data(orderStatus)
                                    .build()
                    );
                case 2:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            ApiResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .message("Thanh toán thất bại")
                                    .data(orderStatus)
                                    .build()
                    );
                case 3:
                    return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                            ApiResponse.builder()
                                    .statusCode(HttpStatus.ACCEPTED.value())
                                    .message("Đơn hàng đang xử lý hoặc chưa thanh toán")
                                    .data(orderStatus)
                                    .build()
                    );
                default:
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                            ApiResponse.builder()
                                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                    .message("Không xác định trạng thái đơn hàng")
                                    .data(orderStatus)
                                    .build()
                    );
            }
        } catch (Exception e) {
            log.error("Failed to query order status for app_trans_id: {}", appTransId, e);
            throw new AppException(ErrorCode.ZALOPAY_ORDER_NOT_FOUND);
        }
    }


    private Map<String, String> buildOrderParams(CreateOrderRequest request, String appTransId, long appTime) {
        Map<String, String> params = new HashMap<>();
        params.put("app_id", ZaloPayConfig.APP_ID);
        params.put("app_user", "ZaloPayDemo");
        params.put("app_time", String.valueOf(appTime));
        params.put("amount", String.valueOf(request.getAmount()));
        params.put("app_trans_id", appTransId);

        // Thêm embed_data với return_url để redirect sau khi thanh toán
        JSONObject embedData = new JSONObject();
        embedData.put("redirecturl", "myapp://payment-result");
        params.put("embed_data", embedData.toString());

        params.put("item", "[]");
        params.put("description", "Payment for order #" + request.getOrderId());
        params.put("callback_url", ZaloPayConfig.REDIRECT_URL);

        String data = params.get("app_id") + "|" + params.get("app_trans_id") + "|" +
                params.get("app_user") + "|" + params.get("amount") + "|" +
                params.get("app_time") + "|" + params.get("embed_data") + "|" +
                params.get("item");
        params.put("mac", HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256,ZaloPayConfig.KEY1, data));

        return params;
    }

    private String sendRequest(String url, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String formData = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        HttpEntity<String> request = new HttpEntity<>(formData, headers);

        return restTemplate.postForObject(url, request, String.class);
    }

    private String generateAppTransId() {
        String datePart = new SimpleDateFormat("yyMMdd").format(new Date());
        String randomPart = UUID.randomUUID().toString().substring(0, 8);
        return datePart + "_" + randomPart;
    }
}
