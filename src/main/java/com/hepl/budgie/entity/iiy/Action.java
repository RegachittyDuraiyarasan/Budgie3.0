package com.hepl.budgie.entity.iiy;

public enum Action {
    INSERT("insert"),
    APPROVE("approve"),
    REJECT("reject"),
    ACTIVE("Active"),
    COURSES_AND_CERTIFICATE("Courses & Certificate"),
    OTHERS("Others"),
    TEAM("Team"),
    OVERALL("Overall"),
    EMPLOYEE("Employee");

    public final String label;

    private Action(String label) {
        this.label = label;
    }
}
