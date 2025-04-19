package com.ptit.booking.service.Impl;

import com.ptit.booking.configuration.ZaloPayConfig;
import com.ptit.booking.constants.SuccessMessage;
import com.ptit.booking.dto.ApiResponse;
import com.ptit.booking.dto.zaloPay.*;
import com.ptit.booking.enums.EnumBookingStatus;
import com.ptit.booking.enums.EnumPaymentType;
import com.ptit.booking.exception.AppException;
import com.ptit.booking.exception.ErrorCode;
import com.ptit.booking.model.Payment;
import com.ptit.booking.repository.BookingRepository;
import com.ptit.booking.repository.PaymentRepository;
import com.ptit.booking.service.ZaloPayService;
import com.ptit.booking.util.HMACUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZaloPayServiceImpl implements ZaloPayService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private HMACUtil hmacUtil;

    @Override
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(CreateOrderRequest request) {
        try {
            String appTransId = generateAppTransId();
            long appTime = System.currentTimeMillis();

            // Lưu giao dịch vào database
            Payment transaction = new Payment();
            transaction.setAppTransId(appTransId);
            transaction.setBooking(bookingRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND)));
            transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
            transaction.setPaymentStatus("PENDING");
            transaction.setPaymentMethod("ZALOPAY");
            transaction.setPaymentType(EnumPaymentType.DEPOSIT.name());
            transaction.setCreatedAt(LocalDateTime.now());
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
            System.out.println("jsonStr: " + dataStr);
            log.info("ZaloPay callback data: {}", dataJson);
            String appTransId = dataJson.getString("app_trans_id");
            int amount = dataJson.getInt("amount");
            String zpTransId = dataJson.getString("zp_trans_id");
            System.out.println("zaloPay callback zpTransId: " + zpTransId);
            log.info("Callback processed for app_trans_id: {}, zp_trans_id: {}", appTransId, zpTransId);
            return Map.of("return_code", 1, "return_message", "Success","zpTransId", zpTransId);
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
            String zpTransId = result.optString("zp_trans_id");
            Payment payment = paymentRepository.findByAppTransId(appTransId);
            payment.setZpTransId(zpTransId);

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
                    payment.setPaymentStatus(EnumBookingStatus.BOOKED.name());
                    paymentRepository.save(payment);
                    return ResponseEntity.ok(
                            ApiResponse.builder()
                                    .statusCode(HttpStatus.OK.value())
                                    .message("Thanh toán thành công")
                                    .data(orderStatus)
                                    .build()
                    );
                case 2:
                    payment.setPaymentStatus("FAIL");
                    paymentRepository.save(payment);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                            ApiResponse.builder()
                                    .statusCode(HttpStatus.BAD_REQUEST.value())
                                    .message("Thanh toán thất bại")
                                    .data(orderStatus)
                                    .build()
                    );
                case 3:
                    payment.setPaymentStatus(EnumBookingStatus.PENDING.name());
                    paymentRepository.save(payment);
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

    @Override
    public RefundResponse refundOrder(RefundOrderRequest request) throws Exception{
        long timestamp = System.currentTimeMillis();
        Random rand = new Random();
        String uid = timestamp + "" + (111 + rand.nextInt(888)); // unique id

        Map<String, Object> payload = new HashMap<>();
        payload.put("app_id", ZaloPayConfig.APP_ID);
        payload.put("m_refund_id", getCurrentTimeString("yyMMdd") +"_"+ ZaloPayConfig.APP_ID +"_"+uid);
        payload.put("zp_trans_id", request.getZpTransId());
        payload.put("amount", request.getAmount());
        payload.put("description", request.getDescription());
        payload.put("timestamp", timestamp);

        String data = payload.get("app_id") +"|"+ payload.get("zp_trans_id") +"|"+ payload.get("amount")
                +"|"+ payload.get("description") +"|"+ payload.get("timestamp");

        payload.put("mac", HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, ZaloPayConfig.KEY1, data));

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(ZaloPayConfig.REFUND_URL);

        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, Object> e : payload.entrySet()) {
            if (e.getValue() != null) {
                params.add(new BasicNameValuePair(e.getKey(), e.getValue().toString()));
            }
        }
        post.setEntity(new UrlEncodedFormEntity(params));

        CloseableHttpResponse res = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
        StringBuilder resultJsonStr = new StringBuilder();
        String line;

        while ((line = rd.readLine()) != null) {
            resultJsonStr.append(line);
        }

        JSONObject result = new JSONObject(resultJsonStr.toString());
        if((int) result.get("return_code") == 1 || (int) result.get("return_code") == 3){
            return RefundResponse.builder()
                    .mRefundId(payload.get("m_refund_id").toString())
                    .refundId(result.get("refund_id").toString())
                    .subReturnCode((int) result.get("sub_return_code"))
                    .returnMessage("Hoàn tiền giao dịch thành công")
                    .returnCode(1)
                    .subReturnMessage("Hoàn tiền giao dịch thành công")
                    .zpTransId(request.getZpTransId())
                    .refundAmount(request.getAmount())
                    .build();
        }
        return RefundResponse.builder()
                .mRefundId(payload.get("m_refund_id").toString())
                .refundId(result.get("refund_id").toString())
                .subReturnCode((int) result.get("sub_return_code"))
                .returnMessage(result.get("return_message").toString())
                .returnCode((int) result.get("return_code"))
                .subReturnMessage(result.get("sub_return_message").toString())
                .zpTransId(request.getZpTransId())
                .refundAmount(request.getAmount())
                .build();
    }

    @Override
    public ResponseEntity<?> getRefundStatus(String mRefundId) throws Exception{
        long timestamp = System.currentTimeMillis();
        String data = ZaloPayConfig.APP_ID + "|" + mRefundId + "|" + timestamp;
        String mac = HMACUtil.HMacHexStringEncode(HMACUtil.HMACSHA256, ZaloPayConfig.KEY1, data);

        Map<String, String> params = new HashMap<>();
        params.put("app_id", ZaloPayConfig.APP_ID);
        params.put("m_refund_id", mRefundId);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("mac", mac);

        String responseStr = sendRequest(ZaloPayConfig.GET_STATUS_REFUND_URL, params);

        JSONObject result = new JSONObject(responseStr);
        RefundStatusResponse response = new RefundStatusResponse();
        response.setReturnCode(result.getInt("return_code"));
        response.setReturnMessage(result.getString("return_message"));
        response.setSubReturnCode(result.getInt("sub_return_code"));
        response.setSubReturnMessage(result.getString("sub_return_message"));

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.builder()
                        .statusCode(HttpStatus.OK.value())
                        .message("Không xác định trạng thái đơn hàng")
                        .data(response)
                        .build()
        );
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
    private static String getCurrentTimeString(String format) {
        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT+7"));
        SimpleDateFormat fmt = new SimpleDateFormat(format);
        fmt.setCalendar(cal);
        return fmt.format(cal.getTimeInMillis());
    }
}
