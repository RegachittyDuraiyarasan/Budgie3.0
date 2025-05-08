package com.hepl.budgie.service.attendancemanagement;

import java.util.*;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.attendancemanagement.AttendanceMusterDTO;
import com.hepl.budgie.dto.attendancemanagement.AttendanceOverride;
import com.hepl.budgie.dto.attendancemanagement.BulkOverrideDTO;
import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.dto.attendancemanagement.MusterHistoryDeleteDto;
import com.hepl.budgie.entity.attendancemanagement.AttendanceMuster;

public interface AttendanceMusterService {

    List<AttendanceMusterDTO> getAttendanceMuster(String empId, String reviewer, String repManager,
            String payrollStatus, String monthYear);

    AttendanceMuster addLopForEmployee(LopDTO lop);

    void saveAttendanceMusterForEmployee(String monthYear, boolean isAll, List<String> empId);

    List<AttendanceMusterDTO> getEmployeeAttendanceMuster(String empId, String monthYear);

    AttendanceMusterDTO employeeMuster(String empId, String monthYear);

    List<Map<String, String>> fetchEmployeeList();

    List<BulkOverrideDTO> getOverrideEmployeeDetails(String empId, String monthYear);

    void updateOverride(AttendanceOverride data);

    Map<String, List<String>> bulkImport(MultipartFile file);

    List<Map<String,Object>> getOverrideHistory(String empId, String monthYear);

    GenericResponse<String> deleteAttendanceMuster(List<MusterHistoryDeleteDto> deleteHistory);
    
}
