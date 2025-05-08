package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.AccountInformationDTO;
import com.hepl.budgie.entity.userinfo.UserInfo;

public interface AccountInformationService {
    UserInfo updateAccountInformation(FormRequest formRequest, String empId);

    AccountInformationDTO getAccountInformation(String empId);
}
