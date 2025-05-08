package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.WorkingInformationDTO;
import com.hepl.budgie.entity.userinfo.UserInfo;

public interface WorkingInformationService {

    UserInfo updateWorkingInformation(FormRequest formRequest, String empId);

    WorkingInformationDTO getWorkingInformation(String empId);
}
