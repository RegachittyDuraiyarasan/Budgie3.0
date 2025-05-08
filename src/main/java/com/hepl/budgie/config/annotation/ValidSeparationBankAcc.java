package com.hepl.budgie.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hepl.budgie.config.annotation.validator.SeparationBankAccValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = SeparationBankAccValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSeparationBankAcc {
    String message() default "{com.hepl.budgie.config.annotation.message}";

    String isSeparationBankAccNew();

    String separationBankAccDetails();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
