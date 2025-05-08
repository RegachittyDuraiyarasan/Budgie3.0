package com.hepl.budgie.controller.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.service.userinfo.ProfileImageService;
import com.hepl.budgie.utils.AppMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/profileImage")
@Slf4j
@RequiredArgsConstructor
public class ProfileImageController {
    private final ProfileImageService profileImageService;
    private final Translator translator;
    private final JWTHelper jwtHelper;

    @PostMapping(value = "/photo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> uploadPhoto(
                                               @RequestParam MultipartFile photo) throws IOException {
        String empid = jwtHelper.getUserRefDetail().getEmpId();
        String response = profileImageService.uploadPhoto(empid, photo);
        return GenericResponse.success(translator.toLocale(AppMessages.PROFILE_UPLOAD));
    }
    @PutMapping(value = "/hrPhoto/{empId}",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String>uploadHrPhoto(@PathVariable String empId,@RequestParam  MultipartFile photo) throws IOException {
        String response = profileImageService.uploadPhoto(empId, photo);
        return GenericResponse.success(translator.toLocale(AppMessages.PROFILE_UPLOAD));
    }
    @PostMapping( value = "/banner", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> bannerImage(
                                               @RequestParam MultipartFile banner) throws IOException {
        String empid = jwtHelper.getUserRefDetail().getEmpId();
        String response = profileImageService.bannerImage(empid, banner);
        return GenericResponse.success(translator.toLocale(AppMessages.PROFILE_BANNER));
    }
    @PutMapping(value = "/hrBanner/{empId}",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String>uploadHrBanner(@PathVariable String empId,@RequestParam  MultipartFile banner) throws IOException {
        String response = profileImageService.bannerImage(empId, banner);
        return GenericResponse.success(translator.toLocale(AppMessages.PROFILE_BANNER));
    }
}
