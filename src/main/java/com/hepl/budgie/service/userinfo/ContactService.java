package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.ContactDTO;
import com.hepl.budgie.entity.userinfo.UserInfo;

public interface ContactService {

    UserInfo updateContact(FormRequest formRequest, String empId);

    ContactDTO getContact(String empId);
}
