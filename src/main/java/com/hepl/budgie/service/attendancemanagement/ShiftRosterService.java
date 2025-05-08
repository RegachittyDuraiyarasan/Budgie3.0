package com.hepl.budgie.service.attendancemanagement;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.entity.attendancemanagement.ShiftRoster;
import com.mongodb.bulk.BulkWriteResult;

import java.io.IOException;

public interface ShiftRosterService {

	List<ShiftRoster> fetch(String monthYear, String empId);

	byte[] shiftRosterTemplate(String monthYear);

	byte[] shiftRosterDayType();

    Map<String, List<String>> importShiftRoaster(MultipartFile file) throws IOException;

    BulkWriteResult excelBulkImport(List<Map<String, Object>> validRows);

}
