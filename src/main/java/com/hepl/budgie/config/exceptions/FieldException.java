package com.hepl.budgie.config.exceptions;

import java.util.HashMap;
import java.util.Map;

public class FieldException extends RuntimeException {

    private final String message;
    private final Map<String, String> errors;
    private Map<String, String[]> errorArgs = new HashMap<>();

    public FieldException(String message, Map<String, String> errors) {
        super(message);
        this.message = message;
        this.errors = errors;
    }

    public FieldException(String message, Map<String, String> errors, Map<String, String[]> errorArgs) {
        super(message);
        this.message = message;
        this.errors = errors;
        this.errorArgs = errorArgs;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    public Map<String, String> getErrors() {
        return this.errors;
    }

    public Map<String, String[]> getErrorArgs() {
        return this.errorArgs;
    }

}
