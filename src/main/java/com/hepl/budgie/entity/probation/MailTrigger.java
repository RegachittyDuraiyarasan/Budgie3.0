package com.hepl.budgie.entity.probation;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class MailTrigger {
    private Boolean day30;
    private Boolean day21;
    private Boolean day15;
    private Boolean day7;
    private List<Map<String, Date>> createdAt;
}
