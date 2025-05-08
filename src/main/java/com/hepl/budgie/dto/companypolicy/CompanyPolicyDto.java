package com.hepl.budgie.dto.companypolicy;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.mail.Multipart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyPolicyDto {
    private String policyCategory;
    private String title;
    private String description;
    private MultipartFile fileUpload;
    private String acknowledgementType;
    private String acknowledgementHeading;
    private String acknowledgementDescription;

}
