package com.hepl.budgie.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hepl.budgie.config.annotation.validator.FileCheckValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

@Target({ FIELD, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FileCheckValidator.class)
public @interface FileChecker {

    String message() default "{com.hepl.budgie.config.validator.message}";

    String ext();

    String allowedFormatArgs();

    boolean isMandatory();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
