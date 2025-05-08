package com.hepl.budgie.config.annotation.validator;

import org.springframework.beans.BeanWrapperImpl;
import com.hepl.budgie.config.annotation.ValidReviewerInfo;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ReviewerInfoValidator implements ConstraintValidator<ValidReviewerInfo, Object> {
    private String shortNoticeDaysField;
    private String waiverField;

    @Override
    public void initialize(ValidReviewerInfo constraintAnnotation) {
        this.shortNoticeDaysField = constraintAnnotation.shortNoticeDays();
        this.waiverField = constraintAnnotation.waiver();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; 
        }

        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        String waiver = (String) wrapper.getPropertyValue(waiverField);
        Integer shortNoticeDays = (Integer) wrapper.getPropertyValue(shortNoticeDaysField);
        context.disableDefaultConstraintViolation();

        if ("yes".equalsIgnoreCase(waiver) && (shortNoticeDays == null || shortNoticeDays <= 0)) {
            context.buildConstraintViolationWithTemplate("Short Notice Days is mandatory when waiver is 'yes'.")
                   .addPropertyNode(shortNoticeDaysField) 
                   .addConstraintViolation();
            return false;
        }

        return true;
    }
}
