package com.hepl.budgie.service.impl.form;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.FormService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class DependencyCheckerServiceImpl implements FormService {
    @Override
    public void validateForm(Object value, FormFieldsDTO masterFormField, Map<String, String> errors, Map<String, String[]> errorArgs) {

    }
}
