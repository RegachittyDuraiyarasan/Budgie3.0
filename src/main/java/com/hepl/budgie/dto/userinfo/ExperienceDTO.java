package com.hepl.budgie.dto.userinfo;

import com.hepl.budgie.entity.userinfo.DocumentDetails;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ExperienceDTO {
    private String experienceId;
    private String jobTitle;
    private String companyName;
    private ZonedDateTime beginOn;
    private Boolean presentOn;
    private ZonedDateTime endOn;
    private DocumentDetails documentDetails;
    private String status;
}
