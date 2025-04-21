package com.ptit.booking.constants;

public class NotificationConstants {
    public static class Template {

        public static class Booking {
            public static final String TITLE_SUCCESS = "Đặt phòng thành công!";
            public static final String MESSAGE_SUCCESS = "Bạn đã đặt thành công khách sạn %s từ %s đến %s.";

            public static final String TITLE_FAIL = "Đặt phòng thất bại";
            public static final String MESSAGE_FAIL = "Đơn đặt phòng tại khách sạn %s đã thất bại. Vui lòng thử lại.";
        }
        public static class Cancel {
            public static final String TITLE_SUCCESS = "Hủy đặt phòng thành công!";
            public static final String MESSAGE_SUCCESS = "Bạn đã hủy đặt phòng thành công khách sạn %s từ %s đến %s.";

            public static final String TITLE_FAIL = "Hủy đặt phòng thất bại";
            public static final String MESSAGE_FAIL = "Bạn đã hủy đặt phòng tại khách sạn %s đã thất bại. Vui lòng thử lại.";
        }

        public static class Checkin {
            public static final String TITLE_SUCCESS = "Checkin phòng thành công!";
            public static final String MESSAGE_SUCCESS = "Bạn đã checkin phòng thành công khách sạn %s từ %s đến %s. Lúc %s";

            public static final String TITLE_FAIL = "Checkin phòng thất bại";
            public static final String MESSAGE_FAIL = "Bạn đã Checkin phòng tại khách sạn %s đã thất bại. Vui lòng thử lại.";
        }
        public static class Checkout {
            public static final String TITLE_SUCCESS = "Checkout phòng thành công!";
            public static final String MESSAGE_SUCCESS = "Bạn đã checkout phòng thành công khách sạn %s từ %s đến %s." +
                    "\n Hãy đánh giá chất lượng khách sạn ngay.";

            public static final String TITLE_FAIL = "Hủy đặt phòng thất bại";
            public static final String MESSAGE_FAIL = "Bạn đã hủy đặt phòng tại khách sạn %s đã thất bại. Vui lòng thử lại.";
        }

        public static class Payment {
            public static final String TITLE_SUCCESS = "Thanh toán thành công!";
            public static final String MESSAGE_SUCCESS = "Bạn đã thanh toán thành công %s cho đơn hàng #%s.";

            public static final String TITLE_FAIL = "Thanh toán thất bại";
            public static final String MESSAGE_FAIL = "Thanh toán cho đơn đặt khách sạn %s đã thất bại. Vui lòng kiểm tra lại.";
        }

        public static class Reminder {

            public static final String TITLE_CHECKIN_TOMORROW = "Bạn đã sẵn sàng cho chuyến đi?";
            public static final String MESSAGE_CHECKIN_TOMORROW = "Bạn sẽ check-in tại khách sạn %s vào ngày mai (%s). Chúc bạn có kỳ nghỉ tuyệt vời!";

            public static final String TITLE_CHECKIN_TODAY = "Đừng quên chuyến đi hôm nay!";
            public static final String MESSAGE_CHECKIN_TODAY = "Bạn sẽ check-in tại khách sạn %s hôm nay (%s). Chúng tôi rất mong được chào đón bạn.";

            public static final String TITLE_CHECKOUT_TOMORROW = "Chuẩn bị cho việc trả phòng?";
            public static final String MESSAGE_CHECKOUT_TOMORROW = "Bạn sẽ check-out khỏi khách sạn %s vào ngày mai (%s). Vui lòng chuẩn bị hành lý của bạn.";

            public static final String TITLE_CHECKOUT_TODAY = "Hôm nay bạn trả phòng";
            public static final String MESSAGE_CHECKOUT_TODAY = "Bạn sẽ check-out khỏi khách sạn %s hôm nay (%s). Hãy kiểm tra lại hành lý trước khi rời đi.";

            public static final String TITLE_REVIEW_REQUEST = "Chia sẻ trải nghiệm của bạn!";
            public static final String MESSAGE_REVIEW_REQUEST = "Bạn vừa checkout khỏi khách sạn %s. Hãy dành chút thời gian để đánh giá chất lượng dịch vụ.";

            public static final String TITLE_PROMO_AVAILABLE = "Bạn có mã ưu đãi chưa dùng!";
            public static final String MESSAGE_PROMO_AVAILABLE = "Đừng bỏ lỡ mã ưu đãi %s – giảm %s cho đơn đặt phòng tại %s. Đặt ngay hôm nay!";

            public static final String TITLE_PROMO_EXPIRING = "Ưu đãi sắp hết hạn!";
            public static final String MESSAGE_PROMO_EXPIRING = "Mã giảm giá %s sẽ hết hạn vào ngày %s. Đặt phòng ngay để không bỏ lỡ cơ hội!";
        }

        public static class Promo {
            public static final String TITLE_PROMO = "Ưu đãi dành riêng cho bạn!";
            public static final String MESSAGE_PROMO = "Giảm ngay %s khi đặt phòng tại %s. Ưu đãi đến hết ngày %s.";
        }

        public static class System {
            public static final String TITLE_MAINTENANCE = "Thông báo bảo trì";
            public static final String MESSAGE_MAINTENANCE = "Hệ thống sẽ tạm dừng từ %s đến %s để bảo trì.";
        }
    }
}
