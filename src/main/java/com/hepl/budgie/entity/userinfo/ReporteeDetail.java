package com.hepl.budgie.entity.userinfo;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ReporteeDetail {
    private String managerId;
    private ZonedDateTime effectiveFrom;
}
