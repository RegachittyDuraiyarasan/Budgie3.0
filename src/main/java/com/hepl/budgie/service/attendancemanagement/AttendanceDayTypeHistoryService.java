package com.hepl.budgie.service.attendancemanagement;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.attendancemanagement.AttendanceDayTypeHistoryDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayTypeHistory;
import com.mongodb.bulk.BulkWriteResult;

public interface AttendanceDayTypeHistoryService {

    AttendanceDayTypeHistory saveAttendanceDayType(AttendanceDayTypeHistoryDTO dayTypeHistory);

    List<AttendanceDayTypeHistory> getAttendanceDayTypeHistory(String empId, String monthYear);

    List<AttendanceDayTypeHistory> getByEmpId(String empId);

    BulkWriteResult excelImports(List<Map<String, Object>> validRows);
    
}
