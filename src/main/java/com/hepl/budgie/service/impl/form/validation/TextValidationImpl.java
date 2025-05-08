package com.hepl.budgie.service.impl.form.validation;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.RegexValidator;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.TextFieldValidation;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextValidationImpl implements TextFieldValidation {

    @Override
    public String validateOrReturnError(FormFieldsDTO masterFormField, Object value) {
        log.info("Validate text");
        if (masterFormField.getRequired().booleanValue() && StringUtils.isEmpty(value.toString())) {
            return AppMessages.FORM_FIELD_REQUIRED;
        }

        if (masterFormField.getValidation() != null) {
            Optional<String> pattern = Optional.ofNullable(masterFormField.getValidation().getPattern());
            if (pattern.isPresent() && !pattern.get().isEmpty()) {
                return validateAgainstPattern((String) value, masterFormField);
            }
        }
        return "";
    }

    private String validateAgainstPattern(String value, FormFieldsDTO masterFormField) {
        RegexValidator validator = new RegexValidator(masterFormField.getValidation().getPattern());
        if (!validator.isValid(value)) {
            return AppMessages.INVALID_PATTERN;
        } else {
            return "";
        }
    }

}
