package com.hepl.budgie.service.leavemanagement;

import java.util.List;

import com.hepl.budgie.dto.leavemanagement.LeaveReportDTO;

public interface LeaveReportService {
    List<LeaveReportDTO> getLeaveReportList(String yearmonth);
}
