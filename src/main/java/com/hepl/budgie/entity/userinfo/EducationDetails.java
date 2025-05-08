package com.hepl.budgie.entity.userinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationDetails {
    private String educationId;
//    private String qualificationId;
    private String qualification;
    private String course;
    private String institute;
    private ZonedDateTime beginOn;
    private ZonedDateTime endOn;
    private String percentageOrCgpa;
    private DocumentDetails documentDetails;
    private String status;
}
