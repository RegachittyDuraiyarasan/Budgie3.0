package com.hepl.budgie.dto.iiy;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class IIYEmployeeDetails {
    private String empName;
    private String reportingManagerEmpId;
    private String reportingManagerName;
    private String reviewerId;
    private String reviewerName;
    private String divisionHeadId;
    private String divisionHeadName;
    private String designation;
    private String department;
    private String dateOfJoining;
    private String groupOfJoining;
    private String status;
}
