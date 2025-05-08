package com.hepl.budgie.service.impl.form.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.TextFieldValidation;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EmailValidationImpl implements TextFieldValidation {

    @Override
    public String validateOrReturnError(FormFieldsDTO masterFormField, Object value) {
        log.info("Validation for email");
        if (masterFormField.getRequired().booleanValue() && StringUtils.isEmpty((String) value)) {
            return AppMessages.FORM_FIELD_REQUIRED;
        }

        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid((String) value)) {
            return AppMessages.FORM_EMAIL_INVALID;
        }
        return "";
    }

}
