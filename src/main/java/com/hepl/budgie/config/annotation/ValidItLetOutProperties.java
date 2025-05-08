package com.hepl.budgie.config.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

import com.hepl.budgie.config.annotation.validator.LetOutPropertiesValidator;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LetOutPropertiesValidator.class)
@Documented
public @interface ValidItLetOutProperties {
    
    String message() default "{validation.itLetOutPropertiesDTO.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
