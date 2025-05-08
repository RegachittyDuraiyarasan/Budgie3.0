package com.hepl.budgie.config.annotation.validator;

import org.springframework.beans.BeanWrapperImpl;

import com.hepl.budgie.config.annotation.ValidSeparationBankAcc;
import com.hepl.budgie.entity.separation.SeparationBankAccDetails;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SeparationBankAccValidator implements ConstraintValidator<ValidSeparationBankAcc, Object> {
    private String isSeparationBankAccNew;
    private String separationBankAccDetails;

    @Override
    public void initialize(ValidSeparationBankAcc constraintAnnotation) {
        this.isSeparationBankAccNew = constraintAnnotation.isSeparationBankAccNew();
        this.separationBankAccDetails = constraintAnnotation.separationBankAccDetails();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Boolean isSeparationBankAccNewValue = (Boolean) new BeanWrapperImpl(value)
                .getPropertyValue(isSeparationBankAccNew);
        SeparationBankAccDetails separationBankAccDetailsValue = (SeparationBankAccDetails) new BeanWrapperImpl(value)
                .getPropertyValue(separationBankAccDetails);

        if (Boolean.TRUE.equals(isSeparationBankAccNewValue)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "separationBankAccDetails should be provided only when isSeparationBankAccNew is true.")
                    .addPropertyNode(isSeparationBankAccNew)
                    .addConstraintViolation();
            return separationBankAccDetailsValue != null;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                "separationBankAccDetails should be provided only when isSeparationBankAccNew is true.")
                .addPropertyNode(separationBankAccDetails)
                .addConstraintViolation();
        return separationBankAccDetailsValue == null;
    }
}
