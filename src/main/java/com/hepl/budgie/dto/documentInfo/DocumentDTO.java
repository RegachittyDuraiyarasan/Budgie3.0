package com.hepl.budgie.dto.documentInfo;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

@Data
public class DocumentDTO {
    
    private String documentsCategory; 
    private String moduleId;
    private String employeesId;
    private String documentsTitle;
    private String documentsDescription;
    private MultipartFile fileUpload;
    private List<MultipartFile> bulkFileUploads;
    private String acknowledgementType;
    private String acknowledgementHeading;
    private String acknowledgementDescription;

    @AssertTrue(message = "{validation.error.notBlank}")
    private boolean isEmployeeId() {
        if (documentsCategory != null && !documentsCategory.isEmpty()) {
            if (documentsCategory.equalsIgnoreCase("normal Upload")) {
                return employeesId != null && !employeesId.isEmpty();
            }
        }

        return true;
    }
}