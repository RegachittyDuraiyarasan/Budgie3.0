package com.hepl.budgie.service.attendancemanagement;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;

public interface ShiftMasterService {

	void addShiftName(FormRequest formRequest);

	void updateShiftType(String id, FormRequest formRequest);

	void deleteShiftType(String id);

	List<Map<String, String>> fetchShiftTypes();

	ShiftMaster addShiftMaster(FormRequest formRequest);

	ShiftMaster updateShiftMaster(String id, FormRequest formRequest);

	void changeStatus(String id, boolean status);

	List<ShiftMaster> fetch();

	byte[] createExcelTemplate();

}
