package com.hepl.budgie.dto.helpdesk;

import lombok.Data;

@Data
public class HrHelpDeskReportDTO {
    private String ticketID;
    private String employeeName;
    private String raisedFor;
    private String spocName;
    private String status;
    private String category;
    private String concerns;
    private String spocRemarks;
//    private String spocSupporting;
    private String raisedDate;
    private String closureDate;
//    private String supporting;
    private String aging;
    private String ticketLifecycle;
}
