package com.hepl.budgie.dto.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EducationInterestingDTO {
    private String qualification;
    private String institute;
    private ZonedDateTime endOn;

}
