package com.hepl.budgie.entity.pms;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;

@Data
public class LevelDetails {
    private String levelId;
    private String levelType;
    private String levelName;
    private Boolean mandatory;
    private List<String> btnLabel;
    private ZonedDateTime btnTimeOut;
    private ZonedDateTime btnTimeStart;
    private List<String> nextLevel;
    private List<String> nextLevelCondition;
    private List<String> previousLevel;
    private List<String> progressStatus;
    private List<String> withDevProgressStatus;
    private String basedOn;
    private List<String> actionCount;
}
