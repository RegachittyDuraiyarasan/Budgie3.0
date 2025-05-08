package com.hepl.budgie.service.iiy;

import com.hepl.budgie.dto.iiy.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface EmployeeService {
    void addActivityWithCertificate(ActivityRequestDTO data, MultipartFile file) throws IOException;

    List<ActivityFetchDTO> fetchActivityList(IIYEmployeeRequestDTO data);

    List<ActivityFetchDTO> fetchTeamActivityList(IIYEmployeeRequestDTO data);

    List<ActivityFetchDTO> fetchTeamActivityReportList(IIYEmployeeRequestDTO data, String type);

    Map<String, List<String>> approveTeamActivity(List<ActivityRequestDTO> data);

    Map<String, List<String>> rejectTeamActivity(List<ActivityRequestDTO> data);

    List<IIYReportFetchDTO> fetchIiyReportList(IIYEmployeeRequestDTO data, String type);

    List<CourseFetchDTO> fetchIiyProcessingCourseListByEmpId(IIYEmployeeRequestDTO data);

}
