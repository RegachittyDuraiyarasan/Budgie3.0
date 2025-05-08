package com.hepl.budgie.dto.helpdesk;

import lombok.Data;

@Data
public class HelpDeskMovementFilterDTO {
    private String startDate;
    private String endDate;
    private String category;
}
