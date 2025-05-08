package com.hepl.budgie.entity.userinfo;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class DivisionHead {
    private String managerId;
    private ZonedDateTime effectiveFrom;
}
