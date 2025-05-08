package com.hepl.budgie.entity.preonboarding;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DayZeroResponse {
    private String empId;
    private String personalMailId;
    private String contactNumber;
    private boolean inductionMailStatus;
    private boolean buddyMailStatus;
}
