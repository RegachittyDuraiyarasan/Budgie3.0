package com.hepl.budgie.entity.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
 public class PreOnboardingProcess {
    private String type;
    private Boolean verified;
    private ZonedDateTime date;
}