package com.hepl.budgie.dto.pms;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class LevelDateDTO {
    private String levelName;
    private List<String> action;
    private List<String> btnLabel;
    private ZonedDateTime actionTimeStart;
    private ZonedDateTime actionTimeOut;
}
