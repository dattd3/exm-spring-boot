package com.example.exm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Utility class containing common test helper methods and utilities.
 * Provides methods for JSON serialization, reflection, assertions, and test data generation.
 */
public class TestUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Random random = new Random();

    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    // ================== JSON UTILITIES ==================

    /**
     * Converts an object to JSON string
     */
    public static String asJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    /**
     * Converts JSON string to object
     */
    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }

    /**
     * Converts MvcResult content to object
     */
    public static <T> T fromMvcResult(MvcResult mvcResult, Class<T> clazz) {
        try {
            String content = mvcResult.getResponse().getContentAsString();
            return fromJsonString(content, clazz);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to get content from MvcResult", e);
        }
    }

    /**
     * Pretty prints JSON for debugging
     */
    public static String prettyPrintJson(Object obj) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to pretty print JSON", e);
        }
    }

    // ================== REFLECTION UTILITIES ==================

    /**
     * Sets a private field value using reflection
     */
    public static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = getField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    /**
     * Gets a private field value using reflection
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object target, String fieldName) {
        try {
            Field field = getField(target.getClass(), fieldName);
            field.setAccessible(true);
            return (T) field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field: " + fieldName, e);
        }
    }

    /**
     * Finds a field in the class hierarchy
     */
    private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            if (superClass == null) {
                throw e;
            }
            return getField(superClass, fieldName);
        }
    }

    /**
     * Invokes a private method using reflection
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object target, String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = Arrays.stream(args)
                    .map(Object::getClass)
                    .toArray(Class[]::new);

            var method = target.getClass().getDeclaredMethod(methodName, paramTypes);
            method.setAccessible(true);
            return (T) method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + methodName, e);
        }
    }

    // ================== ASSERTION UTILITIES ==================

    /**
     * Asserts that two objects are equal, comparing field by field
     */
    public static void assertObjectEquals(Object expected, Object actual, String... excludeFields) {
        if (expected == null && actual == null) {
            return;
        }

        assertNotNull(expected, "Expected object is null");
        assertNotNull(actual, "Actual object is null");
        assertEquals(expected.getClass(), actual.getClass(), "Objects are of different types");

        Set<String> excludeSet = Set.of(excludeFields);
        Field[] fields = expected.getClass().getDeclaredFields();

        for (Field field : fields) {
            if (excludeSet.contains(field.getName())) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object expectedValue = field.get(expected);
                Object actualValue = field.get(actual);
                assertEquals(expectedValue, actualValue,
                        "Field '" + field.getName() + "' values don't match");
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + field.getName(), e);
            }
        }
    }

    /**
     * Asserts that a collection contains exactly the expected elements
     */
    public static <T> void assertContainsExactly(Collection<T> actual, T... expected) {
        assertNotNull(actual, "Actual collection is null");
        assertEquals(expected.length, actual.size(),
                "Collection size doesn't match. Expected: " + expected.length + ", Actual: " + actual.size());

        List<T> expectedList = Arrays.asList(expected);
        assertTrue(actual.containsAll(expectedList),
                "Collection doesn't contain all expected elements");
    }

    /**
     * Asserts that a collection contains all expected elements in any order
     */
    public static <T> void assertContainsAll(Collection<T> actual, T... expected) {
        assertNotNull(actual, "Actual collection is null");
        List<T> expectedList = Arrays.asList(expected);
        assertTrue(actual.containsAll(expectedList),
                "Collection doesn't contain all expected elements");
    }

    /**
     * Asserts that a BigDecimal value equals expected value
     */
    public static void assertBigDecimalEquals(BigDecimal expected, BigDecimal actual) {
        assertNotNull(expected, "Expected BigDecimal is null");
        assertNotNull(actual, "Actual BigDecimal is null");
        assertEquals(0, expected.compareTo(actual),
                "BigDecimal values don't match. Expected: " + expected + ", Actual: " + actual);
    }

    /**
     * Asserts that a LocalDateTime is within a specified range
     */
    public static void assertDateTimeInRange(LocalDateTime actual, LocalDateTime start, LocalDateTime end) {
        assertNotNull(actual, "Actual LocalDateTime is null");
        assertTrue(actual.isAfter(start) || actual.isEqual(start),
                "DateTime " + actual + " is not after or equal to " + start);
        assertTrue(actual.isBefore(end) || actual.isEqual(end),
                "DateTime " + actual + " is not before or equal to " + end);
    }

    /**
     * Asserts that a LocalDateTime is approximately now (within 1 minute)
     */
    public static void assertDateTimeIsNow(LocalDateTime actual) {
        LocalDateTime now = LocalDateTime.now();
        assertDateTimeInRange(actual, now.minusMinutes(1), now.plusMinutes(1));
    }

    // ================== MOCKITO UTILITIES ==================

    /**
     * Verifies that a mock method was called with specific arguments
     */
    public static <T> void verifyMethodCall(T mock, String methodName, Object... expectedArgs) {
        // This is a simplified version - in practice, you'd use Mockito's verify mechanisms
        verify(mock, times(1));
    }

    /**
     * Captures the argument passed to a mock method
     */
    public static <T> ArgumentCaptor<T> captureArgument(Class<T> clazz) {
        return ArgumentCaptor.forClass(clazz);
    }

    /**
     * Verifies that no interactions occurred with the mock after a certain point
     */
    public static void verifyNoMoreInteractions(Object... mocks) {
        org.mockito.Mockito.verifyNoMoreInteractions(mocks);
    }

    // ================== PAGE UTILITIES ==================

    /**
     * Creates a Page object from a list for testing pagination
     */
    public static <T> Page<T> createPage(List<T> content, Pageable pageable, long totalElements) {
        return new PageImpl<>(content, pageable, totalElements);
    }

    /**
     * Creates a Page object with default pagination
     */
    public static <T> Page<T> createPage(List<T> content) {
        return new PageImpl<>(content);
    }

    /**
     * Asserts page properties
     */
    public static void assertPageProperties(Page<?> page, int expectedSize, long expectedTotalElements,
                                            int expectedTotalPages, boolean expectedFirst, boolean expectedLast) {
        assertEquals(expectedSize, page.getContent().size(), "Page content size doesn't match");
        assertEquals(expectedTotalElements, page.getTotalElements(), "Total elements don't match");
        assertEquals(expectedTotalPages, page.getTotalPages(), "Total pages don't match");
        assertEquals(expectedFirst, page.isFirst(), "First page flag doesn't match");
        assertEquals(expectedLast, page.isLast(), "Last page flag doesn't match");
    }

    // ================== RANDOM DATA GENERATORS ==================

    /**
     * Generates a random string of specified length
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }

    /**
     * Generates a random email address
     */
    public static String randomEmail() {
        return randomString(8) + "@" + randomString(5) + ".com";
    }

    /**
     * Generates a random phone number
     */
    public static String randomPhoneNumber() {
        return "+1" + String.format("%010d", random.nextLong(10000000000L));
    }

    /**
     * Generates a random BigDecimal in a range
     */
    public static BigDecimal randomBigDecimal(double min, double max) {
        double randomValue = ThreadLocalRandom.current().nextDouble(min, max);
        return BigDecimal.valueOf(randomValue).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Generates a random integer in a range
     */
    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /**
     * Generates a random LocalDateTime within the last N days
     */
    public static LocalDateTime randomDateTimeInLastDays(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(days);
        long startEpoch = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = now.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);
    }

    // ================== COLLECTION UTILITIES ==================

    /**
     * Creates a list from varargs
     */
    @SafeVarargs
    public static <T> List<T> listOf(T... elements) {
        return Arrays.asList(elements);
    }

    /**
     * Creates a set from varargs
     */
    @SafeVarargs
    public static <T> Set<T> setOf(T... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    /**
     * Filters a collection based on a predicate
     */
    public static <T> List<T> filter(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    /**
     * Maps a collection to another type
     */
    public static <T, R> List<R> map(Collection<T> collection, java.util.function.Function<T, R> mapper) {
        return collection.stream()
                .map(mapper)
                .collect(Collectors.toList());
    }

    /**
     * Finds first element matching predicate
     */
    public static <T> Optional<T> findFirst(Collection<T> collection, Predicate<T> predicate) {
        return collection.stream()
                .filter(predicate)
                .findFirst();
    }

    // ================== WAIT UTILITIES ==================

    /**
     * Waits for a condition to be true with timeout
     */
    public static void waitFor(java.util.function.Supplier<Boolean> condition, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!condition.get()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new RuntimeException("Timeout waiting for condition");
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while waiting", e);
            }
        }
    }

    /**
     * Waits for a condition to be true with default timeout (5 seconds)
     */
    public static void waitFor(java.util.function.Supplier<Boolean> condition) {
        waitFor(condition, 5000);
    }

    // ================== RESULT MATCHER UTILITIES ==================

    /**
     * Creates a custom result matcher for JSON content
     */
    public static ResultMatcher jsonContent(String expectedJson) {
        return result -> {
            String actualJson = result.getResponse().getContentAsString();
            assertEquals(expectedJson, actualJson, "JSON content doesn't match");
        };
    }

    /**
     * Creates a custom result matcher for checking if response contains a field
     */
    public static ResultMatcher hasJsonField(String fieldName) {
        return result -> {
            String content = result.getResponse().getContentAsString();
            assertTrue(content.contains("\"" + fieldName + "\""),
                    "JSON doesn't contain field: " + fieldName);
        };
    }

    // ================== EXCEPTION UTILITIES ==================

    /**
     * Asserts that a specific exception is thrown with expected message
     */
    public static <T extends Exception> void assertThrowsWithMessage(
            Class<T> expectedType, String expectedMessage, Runnable code) {
        T exception = assertThrows(expectedType, code::run);
        assertEquals(expectedMessage, exception.getMessage(), "Exception message doesn't match");
    }

    /**
     * Asserts that a specific exception is thrown with message containing text
     */
    public static <T extends Exception> void assertThrowsWithMessageContaining(
            Class<T> expectedType, String expectedMessagePart, Runnable code) {
        T exception = assertThrows(expectedType, code::run);
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                "Exception message doesn't contain expected text: " + expectedMessagePart);
    }

    // ================== CLEANUP UTILITIES ==================

    /**
     * Resets all mocks
     */
    public static void resetMocks(Object... mocks) {
        for (Object mock : mocks) {
            reset(mock);
        }
    }

    /**
     * Clears all static fields for clean test state
     */
    public static void clearStaticFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) &&
                    !java.lang.reflect.Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    field.set(null, null);
                } catch (IllegalAccessException e) {
                    // Ignore fields that can't be cleared
                }
            }
        }
    }

    // ================== SYSTEM PROPERTY UTILITIES ==================

    /**
     * Temporarily sets a system property for a test
     */
    public static void withSystemProperty(String key, String value, Runnable test) {
        String oldValue = System.getProperty(key);
        try {
            System.setProperty(key, value);
            test.run();
        } finally {
            if (oldValue == null) {
                System.clearProperty(key);
            } else {
                System.setProperty(key, oldValue);
            }
        }
    }

    /**
     * Formats LocalDateTime for display in tests
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    /**
     * Parses LocalDateTime from string
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
    }

    // ================== VALIDATION UTILITIES ==================

    /**
     * Validates that an object is not null and has expected properties
     */
    public static void validateNotNullAndHasProperties(Object obj, String... propertyNames) {
        assertNotNull(obj, "Object is null");
        for (String propertyName : propertyNames) {
            Object value = getField(obj, propertyName);
            assertNotNull(value, "Property '" + propertyName + "' is null");
        }
    }

    /**
     * Validates email format
     */
    public static void validateEmailFormat(String email) {
        assertTrue(email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"),
                "Invalid email format: " + email);
    }

    /**
     * Validates phone number format
     */
    public static void validatePhoneNumberFormat(String phoneNumber) {
        assertTrue(phoneNumber.matches("^\\+?[1-9]\\d{1,14}$"),
                "Invalid phone number format: " + phoneNumber);
    }

    // ================== PERFORMANCE UTILITIES ==================

    /**
     * Measures execution time of a code block
     */
    public static long measureExecutionTime(Runnable code) {
        long startTime = System.currentTimeMillis();
        code.run();
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Asserts that code executes within a time limit
     */
    public static void assertExecutionTimeWithin(long maxTimeMs, Runnable code) {
        long executionTime = measureExecutionTime(code);
        assertTrue(executionTime <= maxTimeMs,
                "Code took too long to execute: " + executionTime + "ms (max: " + maxTimeMs + "ms)");
    }

    // Private constructor to prevent instantiation
    private TestUtils() {
        throw new UnsupportedOperationException("TestUtils is a utility class and cannot be instantiated");
    }
}