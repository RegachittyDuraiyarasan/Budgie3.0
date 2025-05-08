package com.hepl.budgie.config.annotation.validator;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.config.annotation.FileChecker;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileCheckValidator implements ConstraintValidator<FileChecker, MultipartFile> {

    private String extension;
    private boolean isMandatory;

    @Override
    public void initialize(FileChecker constraintAnnotation) {
        this.extension = constraintAnnotation.ext();
        this.isMandatory = constraintAnnotation.isMandatory();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        List<String> allowedExtension = Arrays.stream(extension.split(",")).map(String::trim).toList();
        if (file != null && isMandatory) {
            return allowedExtension.contains(file.getContentType());
        } else {
            return !isMandatory;
        }

    }

}
