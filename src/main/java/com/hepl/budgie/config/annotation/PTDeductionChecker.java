package com.hepl.budgie.config.annotation;

import com.hepl.budgie.config.annotation.validator.PTDeductionCheckerValidation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { PTDeductionCheckerValidation.class })
public @interface PTDeductionChecker {
    String message() default "Invalid deductionMonDetails based on periodicity";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
