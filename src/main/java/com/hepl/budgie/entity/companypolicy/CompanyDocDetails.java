package com.hepl.budgie.entity.companypolicy;

import java.time.ZonedDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyDocDetails {
    private String comDocDetailsId;
    private String policyCategory;
    private String title;
    private String description;
    private ZonedDateTime uploadedOn;
    private String uploadedBy;
    private FileDetails fileDetails;
    private String acknowledgementType;
    private String acknowledgementHeading;
    private String acknowledgementDescription;
    private String status;
}
