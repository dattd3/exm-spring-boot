package com.example.exm.validation;

import com.example.exm.entity.OrderStatus;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class ValidOrderStatusValidator implements ConstraintValidator<ValidOrderStatus, OrderStatus> {

    private List<String> allowedStatuses;

    @Override
    public void initialize(ValidOrderStatus constraintAnnotation) {
        allowedStatuses = Arrays.asList(constraintAnnotation.allowedStatuses());
    }

    @Override
    public boolean isValid(OrderStatus status, ConstraintValidatorContext context) {
        if (status == null) {
            return true; // Let @NotNull handle null validation
        }

        if (allowedStatuses.isEmpty()) {
            return true; // No restrictions
        }

        return allowedStatuses.contains(status.name());
    }
}