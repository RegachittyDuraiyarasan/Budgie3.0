package com.hepl.budgie.entity.master;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.hepl.budgie.entity.userinfo.ProbationSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class MasterFormOptions {

    private String name;
    private String value;
    private Integer noticePeriod;
    private String mobileNumber;
    private String email;
    private String status;
    private String series;
    private String probationStatus;
    private String eligibleForITDeclaration;
    private ProbationSettings probationDetail;
    private String agree;
    private String remarks;
    private String categoryId;
    private String createdBy;
    private String updatedBy;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

}
