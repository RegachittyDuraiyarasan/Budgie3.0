package com.hepl.budgie.entity.userinfo;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class PassportPhoto {
    private String fileName;
    private String folderName;
    private String authorizedBy;
    private ZonedDateTime authorizedOn;
    private String rejectionReason;
    private ZonedDateTime submittedOn;
    private String status;
}
