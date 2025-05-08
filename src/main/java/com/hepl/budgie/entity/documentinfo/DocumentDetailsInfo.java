package com.hepl.budgie.entity.documentinfo;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class DocumentDetailsInfo {
    private String moduleId;
    private String documentCategory;
    private String title;
    private String description;
    private String acknowledgedType;
    private String acknowledgementHeading;
    private String acknowledgementDescription;
    private String status;
    private FileDetails fileDetails;
    private ZonedDateTime createdDate;
    private ZonedDateTime lastModifiedDate;
}
