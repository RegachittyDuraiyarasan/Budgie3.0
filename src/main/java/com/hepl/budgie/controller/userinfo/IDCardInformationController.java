package com.hepl.budgie.controller.userinfo;

import com.google.zxing.WriterException;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.idCard.IdCardGenerationDto;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.IDCardInformationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/card/info")
@Slf4j
@RequiredArgsConstructor
public class IDCardInformationController {
    private final IDCardInformationService service;
    private final MasterFormService masterFormService;
    private final UserInfoRepository userInfoRepository;

    @GetMapping("/{empId}")
    public GenericResponse<IdCardGenerationDto> getIdCardInformation(@PathVariable String empId) {
        IdCardGenerationDto idCardInformation = service.iDCardInformation(empId);
        if (idCardInformation == null) {
            return GenericResponse.error("NO_DATA", "ID Card Information not found for the given user.");
        }
        return GenericResponse.success(idCardInformation);
    }

    @PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<IdCardGenerationDto> updateIdCardInformation(
            @RequestParam String empId,
            @ModelAttribute FormRequest form, @RequestParam String org) {
        Map<String, FormFieldsDTO> formFields = masterFormService.getFormFields(form.getFormName(), org,
                AccessLevelType.ADD);
        masterFormService.formValidate(form, org, AccessLevelType.ADD, formFields);
        UserInfo user = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new RuntimeException("User not found with empId: " + empId));

        try {
            IdCardGenerationDto updatedIdCard = service.updateIdCard(form, user);

            if (updatedIdCard == null) {
                return GenericResponse.error("UPDATE_FAILED", "Failed to update ID Card information.");
            }

            return GenericResponse.success(updatedIdCard);

        } catch (IOException e) {
            return GenericResponse.error("IO_EXCEPTION",
                    "An error occurred while updating the ID Card information: " + e.getMessage());
        } catch (WriterException e) {
            throw new RuntimeException(e);
        }
    }
}
