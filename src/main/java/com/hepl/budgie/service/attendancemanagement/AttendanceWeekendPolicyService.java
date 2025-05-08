package com.hepl.budgie.service.attendancemanagement;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.attendancemanagement.AttendanceWeekendDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;

public interface AttendanceWeekendPolicyService {

    AttendanceWeekendPolicy saveWeekendPolicy(AttendanceWeekendDTO weekend);

    List<AttendanceWeekendPolicy> getWeekendPolicy();

    AttendanceWeekendPolicy getWeekendPolicyByMonth(String month);

    AttendanceWeekendPolicy updateWeekendPolicy(String month, AttendanceWeekendDTO weekend);

    Map<String, Object> getWeekends(String monthYear);
    
}
