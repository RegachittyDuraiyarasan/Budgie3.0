package com.hepl.budgie.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hepl.budgie.config.annotation.validator.MultipleFileCheckerValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.FIELD;

@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MultipleFileCheckerValidator.class)
public @interface MultipleFileChecker {

    String message() default "{com.hepl.budgie.config.annotation.message}";

    String ext();

    boolean isMandatory();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
