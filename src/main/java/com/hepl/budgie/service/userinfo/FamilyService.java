package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.FamilyDTO;
import com.hepl.budgie.entity.userinfo.EmergencyContacts;
import com.hepl.budgie.entity.userinfo.UserInfo;

public interface FamilyService {

    UserInfo insertFamily(FormRequest formRequest, String empId);
    
    UserInfo updateFamily(FormRequest formRequest, String empId);

    FamilyDTO getFamily(String empId);

    UserInfo deleteFamily(EmergencyContacts emergencyContacts, String empId);
}
