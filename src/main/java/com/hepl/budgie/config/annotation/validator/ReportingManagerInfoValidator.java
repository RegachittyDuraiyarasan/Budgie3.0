package com.hepl.budgie.config.annotation.validator;

import java.util.List;

import org.springframework.beans.BeanWrapperImpl;

import com.hepl.budgie.config.annotation.ReportingManagerAttritionValidator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReportingManagerInfoValidator implements ConstraintValidator<ReportingManagerAttritionValidator, Object> {
    private String attritionStatusField;
    private String desirableCriteriaField;
    private String undesirableCriteriaField;
    private String desirableRemarksField;
    private String undesirableRemarksField;

    @Override
    public void initialize(ReportingManagerAttritionValidator constraintAnnotation) {
        this.attritionStatusField = constraintAnnotation.attritionStatus();
        this.desirableCriteriaField = constraintAnnotation.desirableCriteria();
        this.undesirableCriteriaField = constraintAnnotation.undesirableCriteria();
        this.desirableRemarksField = constraintAnnotation.desirableRemarks();
        this.undesirableRemarksField = constraintAnnotation.undesirableRemarks();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);

        String attritionStatus = (String) wrapper.getPropertyValue(attritionStatusField);
        List<String> desirableCriteria = (List<String>) wrapper.getPropertyValue(desirableCriteriaField);
        List<String> undesirableCriteria = (List<String>) wrapper.getPropertyValue(undesirableCriteriaField);
        String desirableRemarks = (String) wrapper.getPropertyValue(desirableRemarksField);
        String undesirableRemarks = (String) wrapper.getPropertyValue(undesirableRemarksField);
        
        context.disableDefaultConstraintViolation(); // Disable default message

        if ("desirable".equalsIgnoreCase(attritionStatus)) {
            if (desirableCriteria == null || desirableCriteria.isEmpty()) {
                context.buildConstraintViolationWithTemplate("Desirable Criteria is required when attrition status is 'desirable'.")
                        .addPropertyNode(desirableCriteriaField)
                        .addConstraintViolation();
                return false;
            }
            if (desirableRemarks == null || desirableRemarks.isEmpty()) {
                context.buildConstraintViolationWithTemplate("Desirable Remarks are required when attrition status is 'desirable'.")
                        .addPropertyNode(desirableRemarksField)
                        .addConstraintViolation();
                return false;
            }
        }

        if ("undesirable".equalsIgnoreCase(attritionStatus)) {
            if (undesirableCriteria == null || undesirableCriteria.isEmpty()) {
                context.buildConstraintViolationWithTemplate("Undesirable Criteria is required when attrition status is 'undesirable'.")
                        .addPropertyNode(undesirableCriteriaField)
                        .addConstraintViolation();
                return false;
            }
            if (undesirableRemarks == null || undesirableRemarks.isEmpty()) {
                context.buildConstraintViolationWithTemplate("Undesirable Remarks are required when attrition status is 'undesirable'.")
                        .addPropertyNode(undesirableRemarksField)
                        .addConstraintViolation();
                return false;
            }
        }
        return true; 
    }
}