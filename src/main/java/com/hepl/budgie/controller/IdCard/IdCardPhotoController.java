package com.hepl.budgie.controller.IdCard;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.idCard.GraphicsTeamIdDTO;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.IdCard.IdCardPhotoService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/idCard")
@Slf4j
@RequiredArgsConstructor
public class IdCardPhotoController {
    private final IdCardPhotoService idCardPhotoService;
    private final Translator translator;
    private final JWTHelper jwtHelper;
    @PostMapping(value = "/idCardPhoto", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> uploadPhoto( @RequestParam String empid,
            @RequestParam MultipartFile idCardPhoto) throws IOException {
        String string =jwtHelper.getUserRefDetail().getEmpId();
        String response = idCardPhotoService.idCardUpload(string,empid, idCardPhoto);
        return GenericResponse.success(translator.toLocale(AppMessages.ID_CARD));
    }

    @GetMapping("/getAll")
    public GenericResponse<List<GraphicsTeamIdDTO>> listGenericResponse(
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String reportingManager,
            @RequestParam(required = false) String dateOfJoining,
            @RequestParam(required = false) String result) {

        String empId = jwtHelper.getUserRefDetail().getEmpId();
        List<GraphicsTeamIdDTO> graphicsTeamIdDTOS = idCardPhotoService.graphicsTeamIdDto(empId, employeeName, reportingManager, dateOfJoining, result);

        return GenericResponse.<List<GraphicsTeamIdDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_STATUS_FETCH))
                .errorType("NONE")
                .data(graphicsTeamIdDTOS)
                .build();
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<byte[]> bulkUpload(
            @RequestParam String action,
            @RequestParam("files") List<MultipartFile> files
    ) throws IOException {
        byte[] excel = idCardPhotoService.bulkUpload(action, files);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "IdCardReport.xlsx");
        return new ResponseEntity<>(excel, headers, HttpStatus.OK);
    }



}
