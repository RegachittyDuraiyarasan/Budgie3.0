package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.ContactDTO;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.ContactService;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/contact-info")
@Slf4j
public class ContactController {

    private final ContactService contactService;

    private final Translator translator;

    private final MasterFormService masterFormService;

    private final JWTHelper jwtHelper;

    public ContactController(ContactService contactService, Translator translator, MasterFormService masterFormService,
            JWTHelper jwtHelper) {
        this.contactService = contactService;
        this.translator = translator;
        this.masterFormService = masterFormService;
        this.jwtHelper = jwtHelper;
    }

    @PutMapping("")
    public GenericResponse<String> updateContact(@RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        contactService.updateContact(formRequest, jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(translator.toLocale(AppMessages.CONTACT_UPDATE));
    }

    @PutMapping("/hr/{empId}")
    public GenericResponse<String> updateHrContact(@PathVariable String empId, @RequestBody FormRequest formRequest) {
        String org = jwtHelper.getOrganizationCode();
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(formRequest.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(formRequest, org, AccessLevelType.ADD, formFields);
        contactService.updateContact(formRequest, empId);
        return GenericResponse.success(translator.toLocale(AppMessages.CONTACT_UPDATE));
    }

    @GetMapping("")
    public GenericResponse<ContactDTO> getContact() {
        ContactDTO contactDTO = contactService.getContact(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(contactDTO);
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<ContactDTO> getHrContact(@PathVariable String empId) {
        ContactDTO contactDTO = contactService.getContact(empId);
        return GenericResponse.success(contactDTO);
    }

}
