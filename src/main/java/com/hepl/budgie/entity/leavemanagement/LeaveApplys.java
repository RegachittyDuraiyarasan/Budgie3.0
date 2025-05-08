package com.hepl.budgie.entity.leavemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
@Data
@Document(collection = "leave_apply")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveApplys {
    @Id
    private String id;
    private String leaveCode;
    private String empId;
    private String appliedTo;
    private String leaveType;
    private String leaveCategory;
    private String childType;
    private String type;
    private List<LeaveDetails> leaveApply;
    private int numOfDays;
    private int balance;
    private String compOffWorkDate;
    private String approverReason;
    private String empReason;
    private String contactNo;
    private String fromDate;
    private String toDate;
    private List<String> appliedCC;
    private String leaveCancel;
    private List<String> file;
    private String expectedDate;
    private String maternityLeaveType;
    private String oldLeaveCode;
    private String status="Pending";
    private LocalDateTime approveOrRejectDate;

//    private String createdBy;
//    private String updatedBy;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;
//    private String leaveCancel=YesNoEnum.NO.getValue();



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LeaveDetails {
        private String date;
        private String fromSession;
        private String toSession;
        private String leaveType;
        private int count;
        private String status;
        private boolean isHalfDay;
        private String approverId;
        private String approverStatus;
        private String approverRemarks;
        private String approvedAt;
    }
}