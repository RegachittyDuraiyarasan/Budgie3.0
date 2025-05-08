package com.hepl.budgie.config.annotation.validator;

import com.hepl.budgie.config.annotation.PTDeductionChecker;
import com.hepl.budgie.dto.payroll.PayrollPtDTO;
import com.hepl.budgie.entity.payroll.payrollEnum.DeductionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import java.util.List;

public class PTDeductionCheckerValidation implements ConstraintValidator<PTDeductionChecker, Object> {

    @Override
    public void initialize(PTDeductionChecker deductionType) {
        System.out.println("Initializer: " + deductionType);
        ConstraintValidator.super.initialize(deductionType);
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Check if the value is null or not of the expected type
        if (!(value instanceof PayrollPtDTO request)) {
            return false;
        }

        String periodicity = request.getPeriodicity();
        Object deductionMonDetails = request.getDeductionMonDetails();

        if (DeductionType.MONTHLY.getLabel().equalsIgnoreCase(periodicity) || DeductionType.YEARLY.getLabel().equalsIgnoreCase(periodicity)) {
            // For "Monthly", deductionMonDetails must be a String
            if (!(deductionMonDetails instanceof String) || !StringUtils.hasText((String) deductionMonDetails)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("For 'Monthly' periodicity, Deduction Month Details must be a non-empty string.")
                        .addConstraintViolation();
                return false;
            }
        } else if (DeductionType.HALF_YEARLY.getLabel().equalsIgnoreCase(periodicity)) {
            // For "Half Yearly", deductionMonDetails must be a List
            if (!(deductionMonDetails instanceof List<?>)) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("For 'Half Yearly' periodicity, Deduction Month Details must be a deduction month cycle.")
                        .addConstraintViolation();
                return false;
            }
        } else {
            // If periodicity is neither "Monthly" nor "Half Yearly", fail validation
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Invalid periodicity value. Must be 'Monthly', 'Yearly' or 'Half Yearly'.")
                    .addConstraintViolation();
            return false;
        }

        // Validation passed
        return true;
    }
}
