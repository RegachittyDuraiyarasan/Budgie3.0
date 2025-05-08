package com.hepl.budgie.service.attendancemanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.attendancemanagement.AttendanceReportDTO;

public interface AttendanceInformationService{

    void attendanceFile() throws Exception;

    List<Map<String, Object>> getEmployeeAttendance(String empId, String currentMonth);

    void processAndSaveAttendanceCitpl();

    Map<String, List<String>> processAttendance(LocalDate date) throws Exception;

    Map<String, List<String>> attendanceBulkUpload(MultipartFile file) throws Exception;

    List<AttendanceReportDTO> getEmployeeAttendanceReport(String empId, LocalDate fromDate, LocalDate toDate);

    void saveAttendance(String location, boolean isSign, String remarks);

    Map<String, Object> getTodayAttendance();
    
}
