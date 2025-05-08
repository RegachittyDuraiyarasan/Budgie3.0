package com.hepl.budgie.service.leavemanagement;

import com.hepl.budgie.dto.leavemanagement.LeaveTypeInfoDTO;
import com.hepl.budgie.dto.leavemanagement.LeaveTypeRequestDTO;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.MongoTemplate;

public interface LeaveTypeCategoryService {
    void add(LeaveTypeRequestDTO leaveTypeCategory);

    List<LeaveTypeCategory> getLeaveTypeCategoryList();
    void deleteLeaveTypeCategory(String id);

    List<LeaveTypeInfoDTO> getLeaveTypeNameList();

	void update(String id, LeaveTypeRequestDTO leaveTypeCategory);
	
	Map<String, String> fetchLeaveTypeCodeMap(String org, MongoTemplate mongoTemplate);
	
	Map<String, String> fetchLeaveTypeNameMap(String org, MongoTemplate mongoTemplate);
}
