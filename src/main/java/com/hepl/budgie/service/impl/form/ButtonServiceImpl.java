package com.hepl.budgie.service.impl.form;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.FormService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ButtonServiceImpl implements FormService {

    @Override
    public void validateForm(Object value, FormFieldsDTO masterFormField, Map<String, String> errors,
            Map<String, String[]> errorArgs) {
        log.info("Button validation");
    }

}
