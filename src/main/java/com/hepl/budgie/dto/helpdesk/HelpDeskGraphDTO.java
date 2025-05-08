package com.hepl.budgie.dto.helpdesk;

import lombok.Data;

@Data
public class HelpDeskGraphDTO {
    private String completed;
    private String pending;
    private String inprogress;
}
