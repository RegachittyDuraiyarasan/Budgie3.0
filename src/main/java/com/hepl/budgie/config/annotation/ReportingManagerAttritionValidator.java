package com.hepl.budgie.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hepl.budgie.config.annotation.validator.ReportingManagerInfoValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Constraint(validatedBy = ReportingManagerInfoValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ReportingManagerAttritionValidator {
    String message() default "Invalid ReportingManagerInfo: Provide desirableCriteria & desirableRemarks if attritionStatus is 'desirable', or undesirableCriteria & undesirableRemarks if 'undesirable'.";
    String attritionStatus();
    String desirableCriteria();
    String desirableRemarks();
    String undesirableCriteria();
    String undesirableRemarks();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
