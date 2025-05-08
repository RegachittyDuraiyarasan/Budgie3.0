package com.hepl.budgie.service.leavemanagement;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.leavemanagement.EmployeeLeaveBalanceReportDTO;
import com.mongodb.bulk.BulkWriteResult;

public interface LeaveBalanceImportService {

	byte[] createExcelTemplate();

	Map<String, List<String>> importLeaveBalance(MultipartFile file) throws IOException;

	byte[] exportLeaveBalance(EmployeeLeaveBalanceReportDTO reportDTO);

	List<String> getLeaveTypeMap();

	BulkWriteResult excelImport(List<Map<String, Object>> validRows);

}
