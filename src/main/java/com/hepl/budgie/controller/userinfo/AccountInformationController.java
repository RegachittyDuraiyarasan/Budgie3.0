package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.AccountInformationDTO;
import com.hepl.budgie.service.userinfo.AccountInformationService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("users/account-info")
@Slf4j
public class AccountInformationController {

    private final AccountInformationService accountInformationService;

    private final Translator translator;

    private final MasterFormService masterFormService;

    private final JWTHelper jwtHelper;

    public AccountInformationController(AccountInformationService accountInformationService, Translator translator,
            MasterFormService masterFormService, JWTHelper jwtHelper) {
        this.accountInformationService = accountInformationService;
        this.translator = translator;
        this.masterFormService = masterFormService;
        this.jwtHelper = jwtHelper;
    }

    @PutMapping("/hr/{empId}")
    public GenericResponse<String> updateAccountInformation(@PathVariable String empId, @RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        accountInformationService.updateAccountInformation(formRequest, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.ACCOUNT_INFORMATION_UPDATE));
    }

    @GetMapping()
    public GenericResponse<AccountInformationDTO> getAccountInformation() {
        AccountInformationDTO accountInformationDTO = accountInformationService.getAccountInformation(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(accountInformationDTO);
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<AccountInformationDTO> getHrAccountInformation(@PathVariable String empId) {
        AccountInformationDTO accountInformationDTO = accountInformationService.getAccountInformation(empId);
        return GenericResponse.success(accountInformationDTO);
    }
}
