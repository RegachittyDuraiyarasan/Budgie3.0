package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_pt")
public class PayrollPt {
    @Id
    private String id;
    private String ptId;
    private String state;
    private String periodicity;
    private String deductionType;
    private Object deductionMonDetails;
    private List<RangeDetails> rangeDetails;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RangeDetails {
        private String rangeId;
        private String gender;
        private Integer salaryFrom;
        private Integer salaryTo;
        private Integer taxAmount;
        private Boolean status = true;
    }
}
