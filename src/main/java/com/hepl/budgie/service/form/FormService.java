package com.hepl.budgie.service.form;

import java.util.Map;

import com.hepl.budgie.dto.form.FormFieldsDTO;

public interface FormService {

    void validateForm(Object value, FormFieldsDTO masterFormField, Map<String, String> errors,
            Map<String, String[]> errorArgs);
}
