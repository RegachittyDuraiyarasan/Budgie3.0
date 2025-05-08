package com.hepl.budgie.service.impl.form;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.FormService;
import com.hepl.budgie.service.form.TextFieldValidation;
import com.hepl.budgie.service.impl.form.validation.ValidationFactory;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TextFieldServiceImpl implements FormService {

    @Override
    public void validateForm(Object value, FormFieldsDTO masterFormField, Map<String, String> errors,
            Map<String, String[]> errorArgs) {
        log.info("Validating text field");

        String inputType = getValidationType(masterFormField);
        TextFieldValidation validation = ValidationFactory.getValidation(inputType)
                .orElseThrow(() -> new CustomResponseStatusException(AppMessages.INVALID_TEXT, HttpStatus.BAD_REQUEST,
                        new Object[] { inputType }));
        String errorMsg = validation.validateOrReturnError(masterFormField, value);

        if (!errorMsg.isEmpty()) {
            errors.put(masterFormField.getFieldId(), errorMsg);
        }

    }

    private String getValidationType(FormFieldsDTO masterFormField) {
        if (masterFormField.getValidation() == null) {
            return "";
        } else {
            return Optional.ofNullable(masterFormField.getValidation().getInputType()).orElse("");
        }
    }

}
