package com.hepl.budgie.entity.probation;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class FollowUp {
    private String results;
    private String extendedMonths;
    private Boolean mailStatus;
    private ZonedDateTime mailSentDate;
}
