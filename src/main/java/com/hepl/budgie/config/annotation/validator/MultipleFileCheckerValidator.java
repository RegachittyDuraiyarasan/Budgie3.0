package com.hepl.budgie.config.annotation.validator;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.config.annotation.MultipleFileChecker;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MultipleFileCheckerValidator implements ConstraintValidator<MultipleFileChecker, List<MultipartFile>> {

    private String extension;
    private boolean isMandatory;

    @Override
    public void initialize(MultipleFileChecker constraintAnnotation) {
        this.extension = constraintAnnotation.ext();
        this.isMandatory = constraintAnnotation.isMandatory();
    }

    @Override
    public boolean isValid(List<MultipartFile> files, ConstraintValidatorContext context) {
        List<String> allowedExtension = Arrays.stream(extension.split(",")).map(String::trim).toList();
        if (files != null) {
            return files.stream().allMatch(
                    file -> allowedExtension.contains(file.getContentType())
                            || (file.getOriginalFilename().isEmpty() && !isMandatory));
        } else {
            return true;
        }

    }

}
