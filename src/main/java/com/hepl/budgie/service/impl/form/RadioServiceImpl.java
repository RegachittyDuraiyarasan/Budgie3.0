package com.hepl.budgie.service.impl.form;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.FormService;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RadioServiceImpl implements FormService {

    @Override
    public void validateForm(Object value, FormFieldsDTO masterFormField, Map<String, String> errors,
            Map<String, String[]> errorArgs) {
        if (masterFormField.getRequired().booleanValue() && StringUtils.isEmpty((String) value)) {
            errors.put(masterFormField.getFieldId(), AppMessages.FORM_FIELD_REQUIRED);
        }
    }

}
