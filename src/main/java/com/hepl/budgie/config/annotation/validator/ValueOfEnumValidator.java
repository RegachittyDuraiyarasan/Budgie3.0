package com.hepl.budgie.config.annotation.validator;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import com.hepl.budgie.config.annotation.ValueOfEnum;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {

    private List<String> acceptedValues;
    private boolean isMandatory;

    @Override
    public void initialize(ValueOfEnum annotation) {
        this.isMandatory = annotation.isMandatory();
        String ext = annotation.method();
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(enumConstant -> {
                    try {
                        Method method = annotation.enumClass().getMethod(ext);
                        return (String) method.invoke(enumConstant);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(
                                "Invalid method or field name specified in @ValueOfEnum: " + ext, e);
                    }
                }).toList();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        } else if (isMandatory && value.toString().trim().isEmpty()) {
            return false;
        }

        return (isMandatory && acceptedValues.contains(value.toString())) || isMandatory == false;
    }

}
