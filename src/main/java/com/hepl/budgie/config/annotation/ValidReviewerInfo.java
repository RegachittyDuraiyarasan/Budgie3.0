package com.hepl.budgie.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hepl.budgie.config.annotation.validator.ReviewerInfoValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = ReviewerInfoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidReviewerInfo {
    String message() default "If waiver is 'yes', shortNoticeDays is mandatory.";
    String shortNoticeDays();
    String waiver();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}