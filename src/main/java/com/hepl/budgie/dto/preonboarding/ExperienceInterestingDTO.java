package com.hepl.budgie.dto.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceInterestingDTO {
    private String companyName;
    private String jobTitle;
    private ZonedDateTime endOn;



}
