package com.ptit.booking.constants;

public class PathConstants {

    public static final String API_V1 = "/api/v1";
    public static final String ORDER = "/order";
    public static final String USER = "/user";
    public static final String PRODUCTS = "/products";


    public static final String INFORMATION = "/information";
    public static final String FORGOT_EMAIL = "/forgot/{email}";
    public static final String RESET = "/reset";
    public static final String CODE = "/{code}";
    public static final String RESET_CODE = RESET + CODE;
    public static final String ACTIVATE_CODE = "/activate" + CODE;
    public static final String EDIT_PASSWORD = "/edit/password";

    public static final String ORDER_ID = "/{orderId}";
    public static final String ORDER_ID_ITEMS = ORDER_ID + "/items";

    public static final String PRODUCT_ID = "/{productId}";

    public static final String IDS = "/ids";
    public static final String SEARCH = "/search";
    public static final String SEARCH_GENDER = SEARCH + "/gender";
    public static final String SEARCH_PRODUCT = SEARCH + "/product";
    public static final String SEARCH_TEXT = SEARCH + "/text";

}