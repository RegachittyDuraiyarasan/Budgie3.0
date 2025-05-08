package com.hepl.budgie.service.impl.form;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.service.form.FormService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.FileExtUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InputFileServiceImpl implements FormService {

    @Override
    public void validateForm(Object value, FormFieldsDTO masterFormField, Map<String, String> errors,
            Map<String, String[]> errorArgs) {
        log.info("Validate files");
        try {
            if (masterFormField.getMultiple().booleanValue()) {
                if (Boolean.TRUE.equals(masterFormField.getRequired()) && value != null) {
                    errors.put(masterFormField.getFieldId(), AppMessages.FORM_FIELD_REQUIRED);
                } else {
                    if (value instanceof List<?>) {
                        List<MultipartFile> files = (List<MultipartFile>) value;
                        for (MultipartFile file : files) {
                            validateFile(file, masterFormField, errors, errorArgs);
                        }
                    } else {
                        MultipartFile file = (MultipartFile) value;
                        validateFile(file, masterFormField, errors, errorArgs);
                    }
                }
            } else {
                MultipartFile file = (MultipartFile) value;
                if (masterFormField.getRequired().booleanValue() && file == null) {
                    errors.put(masterFormField.getFieldId(), AppMessages.FORM_FIELD_REQUIRED);
                } else {
                    validateFile(file, masterFormField, errors, errorArgs);
                }

            }
        } catch (Exception e) {
            errors.put(masterFormField.getFieldId(), AppMessages.FORM_FILE_NOT_SUPPORTED);
            errorArgs.put(masterFormField.getFieldId(),
                    new String[] { FileExtUtils
                            .getHumanReadableFormats(masterFormField.getValidation().getFileType()) });
        }
    }

    private void validateFile(MultipartFile file, FormFieldsDTO masterFormField, Map<String, String> errors,
            Map<String, String[]> errorArgs) throws IOException {
        if (file.getSize() != 0) {
            if (!masterFormField.getValidation().getFileType().contains(file.getContentType())) {
                errors.put(masterFormField.getFieldId(),
                        AppMessages.FORM_FILE_NOT_SUPPORTED);
                errorArgs.put(masterFormField.getFieldId(),
                        new String[] { FileExtUtils
                                .getHumanReadableFormats(masterFormField.getValidation().getFileType()) });

            } else if (file.getBytes().length > masterFormField.getValidation().getMaxFileSize()) {
                errors.put(masterFormField.getFieldId(),
                        AppMessages.FORM_FILE_MAX_SIZE);
                errorArgs.put(masterFormField.getFieldId(),
                        new String[] { FileUtils
                                .byteCountToDisplaySize(
                                        masterFormField.getValidation().getMaxFileSize()) });
            }
        }
    }

}
