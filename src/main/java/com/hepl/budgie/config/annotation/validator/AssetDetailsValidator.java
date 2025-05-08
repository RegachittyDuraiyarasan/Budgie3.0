package com.hepl.budgie.config.annotation.validator;


import org.springframework.beans.BeanWrapperImpl;

import com.hepl.budgie.config.annotation.ValidAssetDetails;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AssetDetailsValidator implements ConstraintValidator<ValidAssetDetails, Object> {
    private String statusField;
    private String valueField;
    private String mandatoryCheckField;

    @Override
    public void initialize(ValidAssetDetails constraintAnnotation) {
        this.statusField = constraintAnnotation.status();
        this.valueField = constraintAnnotation.value();
        this.mandatoryCheckField = constraintAnnotation.mandatoryCheck();
    }
    @Override
    public boolean isValid(Object valuee, ConstraintValidatorContext context) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(valuee);

        String status = (String) wrapper.getPropertyValue(statusField);
        Integer value = (Integer) wrapper.getPropertyValue(valueField);
        String mandatoryCheck = mandatoryCheckField;
        if (valuee == null) {
            return true;
        }
        
        if (mandatoryCheck.equalsIgnoreCase(status) && value <= 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Value must be greater than 0 when status is " + mandatoryCheck)
                   .addPropertyNode(valueField)
                   .addConstraintViolation();
            return false;
        } 
        return true;
    }
}
