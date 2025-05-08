package com.hepl.budgie.entity.workflow;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class ShowOn {
    private String type;
    private ZonedDateTime fromDate;
    private ZonedDateTime toDate;
    private String ref;
    private String id;
    private String from;
    private String to;
    private String fieldId;
}
