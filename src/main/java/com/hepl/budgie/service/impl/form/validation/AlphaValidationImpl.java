package com.hepl.budgie.service.impl.form.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.RegexValidator;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.TextFieldValidation;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AlphaValidationImpl implements TextFieldValidation {

    @Override
    public String validateOrReturnError(FormFieldsDTO masterFormField, Object value) {
        log.info("Validate alphabets");
        if (masterFormField.getRequired().booleanValue() && StringUtils.isEmpty(value.toString())) {
            return AppMessages.FORM_FIELD_REQUIRED;
        }

        RegexValidator validator = new RegexValidator("[a-zA-Z}]+");
        if (!validator.isValid((String) value)) {
            return AppMessages.ALPHA_INVALID;
        }
        return "";
    }

}
