package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.BasicDetailsDTO;
import com.hepl.budgie.entity.userinfo.UserInfo;

public interface BasicDetailsService {
     UserInfo updateBasicDetails(FormRequest formRequest,String empId);

     BasicDetailsDTO getBasicDetails(String empId);
}
