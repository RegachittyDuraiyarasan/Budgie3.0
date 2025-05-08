package com.hepl.budgie.service.leavemanagement;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LeaveGranterTableDTO;

public interface LeaveGranterService {

	void leaveGranter(FormRequest formRequest);

	List<String> fetchLeaveScheme();

	Map<String, Object> fetchPeriodicity(String leaveScheme);

	List<LeaveGranterTableDTO> fetchHistory(String processedType);

}
