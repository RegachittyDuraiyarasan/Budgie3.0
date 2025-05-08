package com.hepl.budgie.controller.userinfo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.userinfo.ForgotPasswordDTO;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.UserService;
import com.hepl.budgie.service.userinfo.UserAuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
@Slf4j
public class ForgotPassowrdController {
    
    private final UserAuthService userAuthService;

   @GetMapping("/getEmailId")
   public GenericResponse<ForgotPasswordDTO> getUserEmailByEmpId(@RequestParam String empId) {
    log.info("Get email id by empId");
    ForgotPasswordDTO forgotPasswordDTO = userAuthService.getUserEmailByEmpId(empId);
    return GenericResponse.success(forgotPasswordDTO);
}
}
