package com.hepl.budgie.dto.leavemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.entity.leavemanagement.LeaveApplys;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class AdminLeaveCalendarDateFilterDTO {
    private String id;
    private String leaveCode;
    private String empId;
    private String empName;
    private String appliedTo;
    private String leaveType;
    private String leaveCategory;
    private String childType;
    private String type;
    private List<LeaveApplys.LeaveDetails> leaveApply;
    private double numOfDays;
    private double balance;
    private String compOffWorkDate;
    private String empReason;
    private String contactNo;
    private String approverReason;
    private List<String> fromToDateList;
    private List<String>  halfDayList;
    private String fromDate;
    private String toDate;
    private String fromSession;
    private String toSession;
    private List<String> appliedCC;
    private List<String> file;
    private String leaveCancel;
    private String expectedDate;
    private String maternityLeaveType;
    private String oldLeaveCode;
    private String status;
    private String approveOrRejectDate;
//    private String createdBy;
//    private String updatedBy;
//    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;

    @Data@JsonInclude(JsonInclude.Include.ALWAYS)
    public static class LeaveApplyDateDetailsDto{
        private String date;
        private String fromSession;
        private String toSession;
        private String leaveType;
        private double count;
        private String approverReason;
        private String status;
    }
}

