package com.hepl.budgie.dto.pms;

import lombok.Data;

import java.util.List;
@Data
public class LevelFlowUpdateDTO {
    private  String levelId;
    private String showType;
    private List<String> status;
    private String completedType;
    private  List<String> completedStatus;
    private String pendingType;
    private  List<String> pendingStatus;
}
