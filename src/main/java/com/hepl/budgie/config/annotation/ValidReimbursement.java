package com.hepl.budgie.config.annotation;

import com.hepl.budgie.config.annotation.validator.StatusValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE}) // Applied to the entire class
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StatusValidator.class) // Custom validator class
public @interface ValidReimbursement {
    String message() default "If status is 0, remark is required and approvedBillAmount must be 0";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
