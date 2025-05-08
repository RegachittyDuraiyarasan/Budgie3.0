package com.hepl.budgie.service.impl.form.validation;

import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

import com.hepl.budgie.entity.InputType;
import com.hepl.budgie.service.form.TextFieldValidation;

public class ValidationFactory {

    static Map<String, TextFieldValidation> validation = new HashMap<>();
    static {
        validation.put(InputType.NUMBER.label, new NumberValidationImpl());
        validation.put(InputType.TEXT.label, new TextValidationImpl());
        validation.put(InputType.ALPHABETIC.label, new TextValidationImpl());
        validation.put(InputType.EMAIL.label, new EmailValidationImpl());
        validation.put(InputType.PHONE_NUMBER.label, new PhoneNumberValidationImpl());
        validation.put(InputType.ALPHANUMERIC.label, new PhoneNumberValidationImpl());
    }

    private ValidationFactory() {
        throw new IllegalStateException("Validation factory class");
    }

    public static Optional<TextFieldValidation> getValidation(String inputType) {
        return Optional
                .ofNullable(inputType.isEmpty() ? validation.get(InputType.TEXT.label) : validation.get(inputType));
    }

}
