package com.hepl.budgie.dto.excel;
import java.util.Map;
import java.util.function.Predicate;

public class ValidationRule {
    private final ContextAwarePredicate predicate;
    private final String errorMessage;

    public ValidationRule(Predicate<Object> predicate, String errorMessage) {
        this.predicate = (value, context) -> predicate.test(value);
        this.errorMessage = errorMessage;
    }

    public ValidationRule(ContextAwarePredicate predicate, String errorMessage) {
        this.predicate = predicate;
        this.errorMessage = errorMessage;
    }

    public boolean isValid(Object value, Map<String, Object> context) {
        return predicate.test(value, context);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @FunctionalInterface
    public interface ContextAwarePredicate {
        boolean test(Object value, Map<String, Object> context);
    }
}