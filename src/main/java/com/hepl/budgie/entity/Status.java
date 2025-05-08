package com.hepl.budgie.entity;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING("Pending"),
    DELETED("Deleted"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    HOLD("Hold"),
    COMPLETED("Completed"),
    WITHDRAWN("Withdrawn"),
    SESSION_1("Session 1"),
    SESSION_2("Session 2"),
    YES("Yes"),
    NO("No"),
    SUCCESS("Success"),
    FAILED("Failed"),
    BIRTHDAY("Birthday"),
    ANNIVERSARY("Anniversary");

    public final String label;

    private Status(String label) {
        this.label = label;
    }
}
