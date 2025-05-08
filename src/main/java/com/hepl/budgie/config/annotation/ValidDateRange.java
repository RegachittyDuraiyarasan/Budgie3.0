package com.hepl.budgie.config.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import com.hepl.budgie.config.annotation.validator.DateRangeValidator;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
