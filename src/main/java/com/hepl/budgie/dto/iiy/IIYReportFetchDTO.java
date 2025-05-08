package com.hepl.budgie.dto.iiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class IIYReportFetchDTO {
    private String empId;
    private IIYEmployeeDetails employeeDetails;
    private String[] courses;
    private String[] coursesCategory;
    private String totalDuration;
    private Map<String, String> coursesCategoryDurations;
    private String remainingDuration;
    private String prorateDuration;
    private String financialYear;

}
