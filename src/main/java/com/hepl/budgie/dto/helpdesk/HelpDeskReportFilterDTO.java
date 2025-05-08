package com.hepl.budgie.dto.helpdesk;

import lombok.Data;

@Data
public class HelpDeskReportFilterDTO {

    private String startDate;
    private String endDate;
    private String category;
    private String status;


}
