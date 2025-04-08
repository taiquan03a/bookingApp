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
