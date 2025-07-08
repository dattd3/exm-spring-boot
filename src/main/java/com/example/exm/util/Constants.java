package com.example.exm.util;

public class Constants {

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int LOW_STOCK_THRESHOLD = 10;

    public static final String SORT_BY_CREATED_DATE = "createdAt";
    public static final String SORT_BY_UPDATED_DATE = "updatedAt";
    public static final String SORT_BY_NAME = "name";
    public static final String SORT_BY_EMAIL = "email";
    public static final String SORT_BY_ORDER_DATE = "orderDate";
    public static final String SORT_BY_TOTAL_AMOUNT = "totalAmount";
    public static final String SORT_BY_PRICE = "price";

    public static final String SORT_DIRECTION_ASC = "asc";
    public static final String SORT_DIRECTION_DESC = "desc";

    private Constants() {
        // Private constructor to prevent instantiation
    }
}