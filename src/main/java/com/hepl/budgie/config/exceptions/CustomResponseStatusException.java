package com.hepl.budgie.config.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomResponseStatusException extends RuntimeException {

    private final String message;
    private final transient Object[] args;
    private final HttpStatus status;

    public CustomResponseStatusException(String message, HttpStatus status, Object[] args) {
        this.message = message;
        this.status = status;
        this.args = args;
    }

}
