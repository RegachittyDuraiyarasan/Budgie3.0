package com.hepl.budgie.entity.userinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceDetails {
    private String experienceId;
    private String jobTitle;
    private String companyName;
    private ZonedDateTime beginOn;
    private Boolean presentOn;
    private ZonedDateTime endOn;
    private DocumentDetails documentDetails;
    private String status;
}
