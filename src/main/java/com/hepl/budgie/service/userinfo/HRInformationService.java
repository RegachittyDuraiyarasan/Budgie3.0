package com.hepl.budgie.service.userinfo;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.DivisionHeadDTO;
import com.hepl.budgie.dto.userinfo.HRInfoDTO;
import com.hepl.budgie.dto.userinfo.PrimaryDTO;
import com.hepl.budgie.dto.userinfo.ReviewerDTO;
import com.hepl.budgie.entity.userinfo.UserInfo;

import java.util.List;

public interface HRInformationService {
    List<PrimaryDTO> getReportingManager();

    List<ReviewerDTO> getReviewer();

    List<DivisionHeadDTO> getDivisionHead();

    UserInfo updateHRInfo(String empId,FormRequest formRequest);

    HRInfoDTO getHRInfo(String empId);
}
