package com.hepl.budgie.dto.helpdesk;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;


@Data
public class HelpdeskTickDTO {
    private String ticketRaisedAt;
    private String category;
    private String details;
    private MultipartFile file;
    private String userName;
    private String employeeCode;
}
