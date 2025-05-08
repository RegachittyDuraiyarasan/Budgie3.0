package com.hepl.budgie.dto.userinfo;

import com.hepl.budgie.entity.userinfo.DocumentDetails;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class EducationDTO {
    private String educationId;
    private String qualification;
    private String course;
    private String institute;
    private ZonedDateTime beginOn;
    private ZonedDateTime endOn;
    private String percentageOrCgpa;
    private DocumentDetails documentDetails;
    private String status;
}
