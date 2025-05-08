package com.hepl.budgie.dto.grade;

import com.hepl.budgie.entity.master.ProbationDetail;
import lombok.Data;

@Data
public class GradeFetchDTO {
    private String gradeId;
    private String gradeName;
    private Integer noticePeriod;
    private String status;
    private Boolean probationStatus;
    private ProbationDetail probationDetail;
}
