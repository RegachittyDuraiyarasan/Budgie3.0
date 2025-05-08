package com.hepl.budgie.config.annotation;

import com.hepl.budgie.config.annotation.validator.ItLetOutValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ItLetOutValidator.class)
@Documented
public @interface ItLetOutValidation {

    String message() default "Either 'itLetOut' or 'itLetOutProperties' must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    
}
