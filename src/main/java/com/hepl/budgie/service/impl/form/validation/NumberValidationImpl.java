package com.hepl.budgie.service.impl.form.validation;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.TextFieldValidation;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NumberValidationImpl implements TextFieldValidation {

    @Override
    public String validateOrReturnError(FormFieldsDTO masterFormField, Object value) {

        if (masterFormField.getRequired().booleanValue() && StringUtils.isEmpty(value.toString())) {
            return AppMessages.FORM_FIELD_REQUIRED;
        }

        if (!NumberUtils.isParsable(value.toString())) {
            return AppMessages.FORM_NUMBER_INVALID;
        }

        return "";
    }

}
