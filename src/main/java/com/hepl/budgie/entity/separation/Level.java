package com.hepl.budgie.entity.separation;

public enum Level {
    EMPLOYEE("Employee"),
    REPORTINGMANAGER("ReportingManager"),
    REVIEWER("Reviewer"),
    ITINFRA("ItInfra"),
    FINANCE("Finance"),
    SITEADMIN("SiteAdmin"),
    HR("HR"),
    SEPARATIONREPORT("SeparationReport"),
    ACCOUNT("Account");


    public final String label;
  
    private Level(String label) {
        this.label = label;
    }
}
