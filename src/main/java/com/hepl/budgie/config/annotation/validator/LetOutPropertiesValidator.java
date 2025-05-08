package com.hepl.budgie.config.annotation.validator;

import com.hepl.budgie.config.annotation.ValidItLetOutProperties;
import com.hepl.budgie.dto.payroll.ItLetOutPropertiesDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LetOutPropertiesValidator implements ConstraintValidator<ValidItLetOutProperties, ItLetOutPropertiesDTO> {

    @Override
    public boolean isValid(ItLetOutPropertiesDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true;
        boolean anyOtherFieldPresent =
                dto.getUnrealizedTax() != 0 ||
                dto.getLoanInterest() != 0 ||
                (dto.getLenderName() != null && !dto.getLenderName().trim().isEmpty()) ||
                (dto.getLenderPan() != null && !dto.getLenderPan().trim().isEmpty()) ||
                dto.getAvailableDate() != null ||
                dto.getDateOfAcquisition() != null;
        if (anyOtherFieldPresent) {
            boolean valid = dto.getLetableAmount() != 0 && dto.getMunicipalTax() != 0;
            if (!valid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Letable amount and Municipal tax are required if any other field is provided")
                        .addConstraintViolation();
            }
            return valid;
        }
        return true;
    }
    
}
