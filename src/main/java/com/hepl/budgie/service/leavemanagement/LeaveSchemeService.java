package com.hepl.budgie.service.leavemanagement;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.leavemanagement.LeaveScheme;

import java.util.List;

public interface LeaveSchemeService {
    String saveForm(FormRequest formRequest);

    List<LeaveScheme> getLeaveSchemeList();
    void deleteLeaveScheme(String id);

	List<String> getLeaveSchemeNames();
}
