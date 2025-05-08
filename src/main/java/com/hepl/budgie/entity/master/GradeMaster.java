package com.hepl.budgie.entity.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "grade")
public class GradeMaster {
    private String gradeId;
    private String gradeName;
    private Integer noticePeriod;
    private String status;
    private Boolean probationStatus;
    private ProbationDetail probationDetail;

}
