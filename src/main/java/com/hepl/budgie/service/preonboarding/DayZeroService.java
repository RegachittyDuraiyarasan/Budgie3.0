package com.hepl.budgie.service.preonboarding;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.preonboarding.DocumentDetailDTO;
import com.hepl.budgie.dto.preonboarding.TodayJoiningDTO;
import com.hepl.budgie.entity.preonboarding.DayZeroResponse;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.UserOtherDocuments;
import java.util.*;

public interface DayZeroService {
    List<DayZeroResponse> fetchDayZeroData();

    List<TodayJoiningDTO> fetchTodayDateOfJoining();

    List<DocumentDetailDTO> getDocuments(String empId) ;

    GenericResponse<Map<String, Object>> toggleFile(String empId, Map<String, String> documentStatusesInput,
            String overallStatus);

    UserInfo getByEmpId(String empId);

    List<UserOtherDocuments> updateVerifiedAt(List<String> empIds);


}