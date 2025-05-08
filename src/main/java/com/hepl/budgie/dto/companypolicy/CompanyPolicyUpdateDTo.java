package com.hepl.budgie.dto.companypolicy;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyPolicyUpdateDTo {
    private String title;
    private String description;
    private MultipartFile fileUpload;
    private String acknowledgementType;
    private String acknowledgementHeading;
    private String acknowledgementDescription;
}
