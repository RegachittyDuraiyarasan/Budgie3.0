package com.hepl.budgie.config.annotation.validator;

import com.hepl.budgie.config.annotation.ValidDateRange;
import com.hepl.budgie.dto.payroll.PayrollHraDTO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, PayrollHraDTO> {

    @Override
    public boolean isValid(PayrollHraDTO dto, ConstraintValidatorContext context) {
        if (dto.getFrom() == null || dto.getTo() == null) return true;

        return dto.getTo().isAfter(dto.getFrom());
    }
}
