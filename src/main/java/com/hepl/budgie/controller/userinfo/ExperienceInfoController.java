package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.userinfo.ExperienceDTO;
import com.hepl.budgie.dto.userinfo.ExperienceRequestDTO;
import com.hepl.budgie.dto.userinfo.UpdateExperienceDTO;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.ExperienceInfoService;
import com.hepl.budgie.utils.AppMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users/exp-info")
@Validated
@Slf4j
public class ExperienceInfoController {

    private final ExperienceInfoService experienceInfoService;

    private final MasterFormService masterFormService;

    private final Translator translator;

    private final JWTHelper jwtHelper;

    @PostMapping()
    public GenericResponse<String> addExperience(@Valid @ModelAttribute ExperienceRequestDTO expDetails,
                                                 @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        log.info("Experience Details: {}", expDetails);
        log.info("Files: {}", files);
        experienceInfoService.addExperience(jwtHelper.getUserRefDetail().getEmpId(),expDetails, files);
        return GenericResponse.success(translator.toLocale(AppMessages.EXPERIENCE_ADDED));
    }

    @PostMapping("/hr/{empId}")
    public GenericResponse<String> addHRExperience(@PathVariable String empId,@Valid @ModelAttribute ExperienceRequestDTO expDetails,
                                                 @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        log.info("Hr Experience Details: {}", expDetails);
        experienceInfoService.addExperience(empId,expDetails, files);
        return GenericResponse.success(translator.toLocale(AppMessages.EXPERIENCE_ADDED));
    }

    @GetMapping()
    public GenericResponse<List<ExperienceDTO>> getExperience() {
        List<ExperienceDTO> experienceDTO = experienceInfoService.getExperience(jwtHelper.getUserRefDetail().getEmpId());
        return GenericResponse.success(experienceDTO);
    }

    @GetMapping("/hr/{empId}")
    public GenericResponse<List<ExperienceDTO>> getHRExperience(@PathVariable String empId) {
        List<ExperienceDTO> experienceDTO = experienceInfoService.getExperience(empId);
        return GenericResponse.success(experienceDTO);
    }

    @PutMapping(value = "/{experienceId}")
    public GenericResponse<String> updateExperience(
            @PathVariable String experienceId,
            @Valid @ModelAttribute UpdateExperienceDTO updateExperienceDTO) {

        log.info("Received HR update request for  experienceId: {}, Data: {}", experienceId, updateExperienceDTO);

        experienceInfoService.updateExperience(jwtHelper.getUserRefDetail().getEmpId(),experienceId, updateExperienceDTO);
        return GenericResponse.success(translator.toLocale(AppMessages.EXPERIENCE_UPDATED));
    }

    @PutMapping(value = "/hr/{empId}/{experienceId}")
    public GenericResponse<String> updateHRExperience(
            @PathVariable String empId,
            @PathVariable String experienceId,
            @Valid @ModelAttribute UpdateExperienceDTO updateExperienceDTO) {

        log.info("Received update request for  experienceId: {}, Data: {}", experienceId, updateExperienceDTO);

        experienceInfoService.updateExperience(empId,experienceId, updateExperienceDTO);
        return GenericResponse.success(translator.toLocale(AppMessages.EXPERIENCE_UPDATED));
    }

    @DeleteMapping()
    public GenericResponse<String> deleteExperience(@RequestBody Map<String, String> request) {
        log.info("Request {}", request);
        String experienceId = request.get("experienceId");
        experienceInfoService.deleteExperience(jwtHelper.getUserRefDetail().getEmpId(),experienceId);
        return GenericResponse.success(translator.toLocale(AppMessages.EXPERIENCE_DELETED));
    }

    @DeleteMapping("/hr")
    public GenericResponse<String> deleteHRExperience(@RequestBody Map<String, String> request) {
        String empId = request.get("empId");
        String experienceId = request.get("experienceId");
        experienceInfoService.deleteExperience(empId,experienceId);
        return GenericResponse.success(translator.toLocale(AppMessages.EXPERIENCE_DELETED));
    }


}
