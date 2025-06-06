package com.hepl.budgie.config.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hepl.budgie.config.annotation.validator.ValueOfEnumValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.TYPE_USE;

@Target({ PARAMETER, METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { ValueOfEnumValidator.class })
public @interface ValueOfEnum {
    Class<? extends Enum<?>> enumClass();

    String args() default "";

    boolean isMandatory() default true;

    String method() default "getLabel";

    String message() default "{com.hepl.budgie.config.annotation.validator.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
