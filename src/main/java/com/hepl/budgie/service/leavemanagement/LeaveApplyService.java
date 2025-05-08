package com.hepl.budgie.service.leavemanagement;

import com.hepl.budgie.dto.form.FormRequest;

import com.hepl.budgie.entity.leavemanagement.LeaveApply;
import jakarta.validation.Valid;

import java.util.List;

public interface LeaveApplyService {
    void add(@Valid FormRequest request);

    void update(@Valid FormRequest request);

    List<LeaveApply> getFilteredLeaveApplications(String role);
}
