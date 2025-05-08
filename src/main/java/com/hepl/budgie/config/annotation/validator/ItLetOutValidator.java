package com.hepl.budgie.config.annotation.validator;

import com.hepl.budgie.dto.payroll.LetOutDTO;

import com.hepl.budgie.config.annotation.ItLetOutValidation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItLetOutValidator implements ConstraintValidator<ItLetOutValidation, LetOutDTO> {

     @Override
    public boolean isValid(LetOutDTO dto, ConstraintValidatorContext context) {
        boolean hasItLetOut = dto.getItLetOut() != null;
        boolean hasProperties = dto.getItLetOutProperties() != null && !dto.getItLetOutProperties().isEmpty();

        return hasItLetOut || hasProperties;
    }
    
}
