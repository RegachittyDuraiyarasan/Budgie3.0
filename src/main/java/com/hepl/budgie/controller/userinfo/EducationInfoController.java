package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.userinfo.EducationDTO;
import com.hepl.budgie.dto.userinfo.EducationRequestDTO;
import com.hepl.budgie.dto.userinfo.UpdateEducationDTO;
import com.hepl.budgie.service.userinfo.EducationInfoService;
import com.hepl.budgie.utils.AppMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/edu-info")
@Validated
@Slf4j
public class EducationInfoController {
    private final EducationInfoService educationInfoService;

    private final Translator translator;

    private final JWTHelper jwtHelper;

    @PostMapping()
    public GenericResponse<String> addEducation(@Valid @ModelAttribute EducationRequestDTO eduDetails,
                                                 @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        log.info("Education Details: {}", eduDetails);
        log.info("Files: {}", files);
        educationInfoService.addEducation(jwtHelper.getUserRefDetail().getEmpId(),eduDetails,files);
        return GenericResponse.success(translator.toLocale(AppMessages.EDUCATION_ADDED));
    }

    @PostMapping("/hr/{empId}")
    public GenericResponse<String> addHREducation(@PathVariable String empId,@Valid @ModelAttribute EducationRequestDTO eduDetails,
                                                @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        log.info("HR Education Details: {}", eduDetails);
        educationInfoService.addEducation(empId,eduDetails,files);
        return GenericResponse.success(translator.toLocale(AppMessages.EDUCATION_ADDED));
    }

    @GetMapping()
    public GenericResponse<List<EducationDTO>> getEducation(){
        List<EducationDTO> educationDTO = educationInfoService.getEducation(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(educationDTO);
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<List<EducationDTO>> getHREducation(@PathVariable String empId){
        List<EducationDTO> educationDTO = educationInfoService.getEducation(empId);
        return GenericResponse.success(educationDTO);
    }

    @PutMapping(value = "/{educationId}")
    public GenericResponse<String> updateEducation(
            @PathVariable String educationId,
            @Valid @ModelAttribute UpdateEducationDTO eduDetails) {

        log.info("Received update request for  educationId: {}, Data: {}", educationId, eduDetails);

        educationInfoService.updateEducation(jwtHelper.getUserRefDetail().getEmpId(),educationId, eduDetails);
        return GenericResponse.success(translator.toLocale(AppMessages.EDUCATION_UPDATED));
    }

    @PutMapping(value = "/hr/{empId}/{educationId}")
    public GenericResponse<String> updateHREducation(
            @PathVariable String empId,
            @PathVariable String educationId,
            @Valid @ModelAttribute UpdateEducationDTO eduDetails) {

        log.info("Received update request for HR educationId: {}, Data: {}", educationId, eduDetails);

        educationInfoService.updateEducation(empId,educationId, eduDetails);
        return GenericResponse.success(translator.toLocale(AppMessages.EDUCATION_UPDATED));
    }

    @DeleteMapping()
    public GenericResponse<String> deleteEducation(@RequestBody Map<String, String> request) {
        log.info("Education Request {}", request);
        String educationId = request.get("educationId");
        educationInfoService.deleteEducation(jwtHelper.getUserRefDetail().getEmpId(),educationId);
        return GenericResponse.success(translator.toLocale(AppMessages.EDUCATION_DELETED));
    }

    @DeleteMapping("/hr")
    public GenericResponse<String> deleteHREducation(@RequestBody Map<String, String> request) {
        String empId = request.get("empId");
        String educationId = request.get("educationId");
        educationInfoService.deleteEducation(empId,educationId);
        return GenericResponse.success(translator.toLocale(AppMessages.EDUCATION_DELETED));
    }
}
