package com.hepl.budgie.dto.documentInfo;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class ResponseDocumentDTO {
    private String empId;
    private String userName;
    private String moduleId;
    private String documentCategory;
    private String title;
    private String description;
    private String acknowledgedType;
    private String acknowledgementHeading;
    private String acknowledgementDescription;
    private String status;
    private String folderName;
    private String fileName;
    private String createdDate;
    private String lastModifiedDate;
}
