package com.hepl.budgie.service.form;

import com.hepl.budgie.dto.form.FormFieldsDTO;

public interface TextFieldValidation {

    String validateOrReturnError(FormFieldsDTO masterFormField, Object value);

}
