package com.hepl.budgie.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PmsLevelAndAction {
    EMPLOYEE("Employee"),
    REPORTING_MANAGER("Reporting Manager"),
    REVIEWER("Reviewer"),
    ACTION_MULTIPLE("Multiple"),
    ACTION_SINGLE("Single"),
    KEY_CUSTOMER("customer"),
    KEY_PROCESS("process"),
    KEY_PEOPLE("people");
    public final String label;

}
