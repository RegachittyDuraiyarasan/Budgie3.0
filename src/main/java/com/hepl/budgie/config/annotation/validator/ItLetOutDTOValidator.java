package com.hepl.budgie.config.annotation.validator;

import com.hepl.budgie.config.annotation.ValidItLetOut;
import com.hepl.budgie.dto.payroll.ItLetOutDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ItLetOutDTOValidator implements ConstraintValidator<ValidItLetOut, ItLetOutDTO> {

    @Override
    public boolean isValid(ItLetOutDTO dto, ConstraintValidatorContext context) {
        if (dto == null) return true; 

        boolean anyFieldPresent = isNotBlank(dto.getName())
                || dto.getDeclaredAmount() != 0
                || isNotBlank(dto.getPan())
                || dto.getDateofAvailing() != null
                || dto.getDateOfAcquisition() != null;

        if (!anyFieldPresent) return true; 

        boolean allPresent = isNotBlank(dto.getName())
                && dto.getDeclaredAmount() != 0
                && isNotBlank(dto.getPan())
                && dto.getDateofAvailing() != null
                && dto.getDateOfAcquisition() != null;

        if (!allPresent) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "All fields in itLetOut must be provided if any one is filled"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
