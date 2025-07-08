package com.example.exm.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class OrderNumberGenerator {

    private static final String PREFIX = "ORD";
    private static final Random random = new Random();

    public String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomSuffix = String.format("%03d", random.nextInt(1000));
        return PREFIX + timestamp + randomSuffix;
    }
}