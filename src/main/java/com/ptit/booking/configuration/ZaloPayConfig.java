package com.ptit.booking.configuration;



public class ZaloPayConfig {
    public static String APP_ID = "2553";
    public static String KEY1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
    public static String KEY2 = "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz";
    public static String CREATE_ORDER_URL = "https://sb-openapi.zalopay.vn/v2/create";
    public static String GET_STATUS_PAY_URL = "https://sb-openapi.zalopay.vn/v2/query";
    public static String REDIRECT_URL = "https://f110-2405-4802-1d25-f5d0-5c29-bfeb-e8ea-5f33.ngrok-free.app/api/payment/callback";
    public static String REFUND_URL = "https://sb-openapi.zalopay.vn/v2/refund";
    public static String GET_STATUS_REFUND_URL = "https://sb-openapi.zalopay.vn/v2/query_refund";
}
