package com.ptit.booking.constants;


public class ErrorMessage {
    public static final String INVALID_JWT_TOKEN = "JWT token is expired or invalid";
    public static final String EMAIL_NOT_FOUND = "Email not found";
    public static final String EXISTED_USER_NAME = "User name existed, please try another user name!";
    public static final String INCORRECT_PASSWORD_OR_EMAIL = "Email hoặc mật khẩu sai!";
    public static final String INCORRECT_PASSWORD = "Mật khẩu sai.";
    public static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match.";
    public static final String EMAIL_IN_USE = "Email đã được sử dụng. Vui lòng sử dụng email khác!";
    public static final String INACTIVE = "Account is inactivate";
    public static final String EMPTY_PASSWORD_CONFIRMATION = "Password confirmation cannot be empty.";
    public static final String INCORRECT_PASSWORD_CONFIRMATION = "Password confirmation cannot be empty.";
    public static final String NEW_PASSWORD_IS_SAME_CURRENT_PASSWORD = "Password mới trùng với password hiện tại, vui lòng thử một password khác";
    public static final String EMAIL_OR_PHONE_NOT_FOUND = "Email or phone not found";
    public static final String CHECKIN_AFTER_CHECKOUT = "Ngày trả phòng phải sau ngày nhận phòng";
    public static final String CHECKIN_MUST_TODAY_OR_FUTURE = "Ngày check-in phải là ngày hôm nay hoặc là tương lại.";
    public static final String PLEASE_LOGIN = "Vui lòng đăng nhập trước khi sử dụng tích năng này.";
    public static  String ROOM_NOT_IN_HOTEL(String hotelName){
        return "Phòng mà bạn chọn không thuộc khách sạn "+ hotelName +
                ".\n Vui lòng lựa chọn phòng phù hợp khác.";
    }
    public static final String HOTEL_NOT_BOOKED = "Bạn chưa đặt khách sạn này.";
    public static final String REFUND_BOOKING_FAIL = "Hoàn tiền thất bại.";
    public static final String CURRENT_TIME_MORE_THAN_CHECKIN = "Yêu cầu hủy thất bại. Thời gian hiện tại sau hoặc bằng thời gian check in.";
    public static final String BOOKING_IS_CANCELED = "Bạn đã hủy đơn đặt này rồi.";
    public static final String CHECKIN_FAILED = "Check-in failed. Chưa đến thời gian check in.";
    public static final String COUPON_NOT_IN_RANK = "Mã khuyến mãi này không thuộc hạng thành viên của bạn.";
    public static final String BOOKING_NOT_CHECKOUT = "Vui lòng checkout phòng trước khi đánh giá.";
    public static final String BOOKING_IS_NOT_HOTEL = "Khách sạn bạn đặt khác với khách sạn bạn đánh giá.";
    public static final String MAXIMUM_5_IMAGE = "Chỉ được tối đa 5 ảnh.";
}
