package com.hepl.budgie.entity.menu;

import lombok.Getter;

@Getter
public enum ConditionType {

    REPORTING_MANAGER("reportingManager"),
    BOARDING("onboradingStatus"),
    SPOC("spocStatus"),
    REVIEWER("reviewer"),
    SPOC_REPORTING("spocReportingManagerStatus");

    public final String label;

    private ConditionType(String label) {
        this.label = label;
    }

}
