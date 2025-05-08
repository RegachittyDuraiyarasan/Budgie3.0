package com.hepl.budgie.service.impl.form;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.FormService;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CheckboxServiceImpl implements FormService {

    @Override
    public void validateForm(Object value, FormFieldsDTO masterFormField, Map<String, String> errors,
            Map<String, String[]> errorArgs) {
        log.info("Validating checkbox");
        try {
            if (masterFormField.getMultiple().booleanValue()) {
                @SuppressWarnings("unchecked")
                List<String> values = (List<String>) value;
                if (masterFormField.getRequired().booleanValue() && values.isEmpty()) {
                    errors.put(masterFormField.getFieldId(), AppMessages.FORM_FIELD_REQUIRED);
                }
            } else {
                if (masterFormField.getRequired().booleanValue() && value == null) {
                    errors.put(masterFormField.getFieldId(), AppMessages.FORM_FIELD_REQUIRED);
                }
            }
        } catch (ClassCastException e) {
            errors.put(masterFormField.getFieldId(), AppMessages.FORM_FIELD_REQUIRED);
        }
    }

}
