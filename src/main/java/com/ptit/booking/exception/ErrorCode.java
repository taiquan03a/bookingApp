package com.ptit.booking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    NOT_FOUND(404, "Not found", HttpStatus.NOT_FOUND),
    BOOKING_ROOM_NOT_FOUND(405, "Booking room not found", HttpStatus.NOT_FOUND),
    SERVICE_NOT_FOUND(406, "ServiceEntity not found", HttpStatus.NOT_FOUND),
    EMAIL_OR_PHONE_NOT_FOUND(404, "Email or Phone number not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    FIREBASE_KEY_INVALID(403, "Firebase key invalid", HttpStatus.BAD_REQUEST),
    FIREBASE_KEY_EXPIRED(401, "Firebase key expired", HttpStatus.BAD_REQUEST),
    LOCATION_NOT_FOUND(404, "Location not found", HttpStatus.NOT_FOUND),
    ROOM_NOT_FOUND(404, "Room not found", HttpStatus.NOT_FOUND),
    HOTEL_NOT_FOUND(404, "Hotel not found", HttpStatus.NOT_FOUND),
    ZALOPAY_ORDER_CREATION_FAILED(2001, "Failed to create ZaloPay order", HttpStatus.BAD_REQUEST),
    ZALOPAY_INVALID_CALLBACK(2002, "Invalid ZaloPay callback", HttpStatus.BAD_REQUEST),
    ZALOPAY_ORDER_NOT_FOUND(2003, "ZaloPay order not found", HttpStatus.NOT_FOUND),
    HOTEL_NOTFOUND(2004, "Hotel not found", HttpStatus.NOT_FOUND),
    COUPON_NOT_FOUND(2005, "Coupon not found", HttpStatus.NOT_FOUND),
    NOTIFICATION_BAD(2006, "Notification not found", HttpStatus.BAD_REQUEST),
    NOTIFICATION_FAILED(2007, "Notification failed", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(2008, "Booking not found", HttpStatus.NOT_FOUND),
    PAYMENT_DEPOSIT_NOT_FOUND(2009, "Payment deposit not found", HttpStatus.NOT_FOUND),
    REVIEW_NOT_FOUND(2010, "Review not found", HttpStatus.NOT_FOUND),
    CHECKIN_NOW(2011, "Ngày nhận phòng không được trước hôm nay.", HttpStatus.BAD_REQUEST),
    CHECKOUT_AFTER_CHECKIN(2012, "Ngày trả phòng phải sau ngày nhận phòng.", HttpStatus.BAD_REQUEST),
    DURATION_NOT_30(2013,"Thời gian lưu trú không được vượt quá 30 ngày.", HttpStatus.BAD_REQUEST),
    IMAGE_HOTEL_NOT_FOUND(2014,"image hotel not found", HttpStatus.NOT_FOUND),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
