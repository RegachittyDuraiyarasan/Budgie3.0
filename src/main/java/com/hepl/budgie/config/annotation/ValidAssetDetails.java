package com.hepl.budgie.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hepl.budgie.config.annotation.validator.AssetDetailsValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = AssetDetailsValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAssetDetails {
    String message() default "Value is mandatory when status is 'No'";
    String status();
    String value();
    String mandatoryCheck() default "No";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}