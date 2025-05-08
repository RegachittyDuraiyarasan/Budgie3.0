package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.userinfo.ForgotPasswordDTO;
import com.hepl.budgie.dto.userlogin.LoginResponse;
import com.hepl.budgie.dto.userlogin.UserLogin;
import com.hepl.budgie.dto.userlogin.UserSwitchAuth;

public interface UserAuthService {

    LoginResponse authUser(UserLogin userLogin);

    LoginResponse switchUser(UserSwitchAuth switchAuth);

    ForgotPasswordDTO getUserEmailByEmpId(String empId);
}
