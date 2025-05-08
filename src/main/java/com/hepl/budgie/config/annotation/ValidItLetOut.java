package com.hepl.budgie.config.annotation;


import java.lang.annotation.*;

import com.hepl.budgie.config.annotation.validator.ItLetOutDTOValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ItLetOutDTOValidator.class)
@Documented
public @interface ValidItLetOut {
    
    String message() default "{valdation.itLetOutDTO.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
