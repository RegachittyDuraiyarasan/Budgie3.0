package com.hepl.budgie.entity.workflow;

import lombok.Data;
import java.util.List;
@Data
public class InvokeMethod {
    private String function;
    private List<String> argumentId;
    private String updateToId;
    private Integer showOnDaysBefore;
    private Integer showOnDaysAfter;

}
