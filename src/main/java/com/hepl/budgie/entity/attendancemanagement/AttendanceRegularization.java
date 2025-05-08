package com.hepl.budgie.entity.attendancemanagement;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "attendance_regularization")
public class AttendanceRegularization extends AuditInfo {

    @Id
    private String id;
    private String employeeId;
    private String employeeName;
    private String regularizationCode;
    private List<AppliedRegularization> appliedRegularizations;
    private int noOfDays;
    private List<String> regularizationDates;
    private List<String> approvedDates;
    private List<String> rejectedDates;
    private String status;
    private LocalDate appliedDate;
    private String appliedTo;
    private String remarks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
