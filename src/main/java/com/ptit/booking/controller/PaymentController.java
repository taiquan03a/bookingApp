package com.ptit.booking.controller;

import com.ptit.booking.configuration.ZaloPayConfig;
import com.ptit.booking.dto.booking.BookingRoomRequest;
import com.ptit.booking.dto.zaloPay.CreateOrderRequest;
import com.ptit.booking.dto.zaloPay.RefundOrderRequest;
import com.ptit.booking.dto.zaloPay.RefundResponse;
import com.ptit.booking.enums.EnumBookingStatus;
import com.ptit.booking.model.Booking;
import com.ptit.booking.model.Payment;
import com.ptit.booking.repository.BookingRepository;
import com.ptit.booking.repository.PaymentRepository;
import com.ptit.booking.service.PaymentService;
import com.ptit.booking.service.ZaloPayService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.xml.bind.DatatypeConverter;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Map;
import java.util.logging.Logger;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    private Logger logger = Logger.getLogger(this.getClass().getName());
    private Mac HmacSHA256;
    private final ZaloPayService zaloPayService;

    @PostMapping("checkout")
    public ResponseEntity<?> createPayment(@RequestBody BookingRoomRequest bookingRoomRequest, Principal principal) {
        return paymentService.checkout(bookingRoomRequest,principal);
    }

    @PostMapping("create_order")
    @Operation(summary = "API TEST",description = "Chi là api để back-end test ko sử dụng cho front-end")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest createOrderRequest) {
        return zaloPayService.createOrder(createOrderRequest);
    }

    @GetMapping("status")
    public ResponseEntity<?> getStatus(@RequestParam String appTransId) {
        return zaloPayService.getOrderStatus(appTransId);
    }

    @PostMapping("refund")
    @Operation(summary = "API TEST",description = "Chi là api để back-end test ko sử dụng cho front-end")
    public RefundResponse refund(@RequestBody RefundOrderRequest request) throws Exception {
        return zaloPayService.refundOrder(request);
    }

    @GetMapping("check_refund")
    public ResponseEntity<?> checkRefund(@RequestParam String mRefundId) throws Exception {
        return zaloPayService.getRefundStatus(mRefundId);
    }

    @PostMapping("/callback")
    @Operation(summary = "API TEST",description = "Chi là api để back-end test ko sử dụng cho front-end")
    public String callback(@RequestBody String jsonStr) throws NoSuchAlgorithmException, InvalidKeyException {
        System.out.println("<------------ Callback is called ------------->");
        JSONObject result = new JSONObject();
        HmacSHA256 = Mac.getInstance("HmacSHA256");
        HmacSHA256.init(new SecretKeySpec(ZaloPayConfig.KEY2.getBytes(), "HmacSHA256"));
        try {
            JSONObject cbdata = new JSONObject(jsonStr);
            String dataStr = cbdata.getString("data");
            String reqMac = cbdata.getString("mac");

            byte[] hashBytes = HmacSHA256.doFinal(dataStr.getBytes());
            String mac = DatatypeConverter.printHexBinary(hashBytes).toLowerCase();

            // kiểm tra callback hợp lệ (đến từ ZaloPay server)
            if (!reqMac.equals(mac)) {
                // callback không hợp lệ
                result.put("return_code", -1);
                result.put("return_message", "mac not equal");
            } else {
                // thanh toán thành công
                // merchant cập nhật trạng thái cho đơn hàng
                JSONObject data = new JSONObject(dataStr);
                logger.info("data: " + data.toString());
//                logger.info("update order's status = success where app_trans_id = " + data.getString("app_trans_id"));
                Payment payment = paymentRepository.findByAppTransId(data.getString("app_trans_id"));
                String zpTransId = data.optString("zp_trans_id");
                payment.setZpTransId(zpTransId);
                payment.setPaymentStatus(EnumBookingStatus.BOOKED.name());
                paymentRepository.save(payment);
                Booking booking = payment.getBooking();
                booking.setStatus(EnumBookingStatus.BOOKED.name());
                bookingRepository.save(booking);
                result.put("return_code", 1);
                result.put("return_message", "success");
            }
        } catch (Exception ex) {
            result.put("return_code", 0); // ZaloPay server sẽ callback lại (tối đa 3 lần)
            result.put("return_message", ex.getMessage());
        }

        // thông báo kết quả cho ZaloPay server
        return result.toString();
    }
}
