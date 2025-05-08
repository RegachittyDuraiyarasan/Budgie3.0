package com.hepl.budgie.dto.companypolicy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyPolicyResponseDto {
    private String comDocDetailsId;
    private String policyCategory;
    private String title;
    private String description;
    private String folderName;
    private String fileName;
    private String status;
    private String acknowledgementType;
    private String acknowledgementHeading;
    private String acknowledgementDescription;
}
