package com.hepl.budgie.controller.preonboarding;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.preonboarding.DocumentDetailDTO;
import com.hepl.budgie.dto.preonboarding.EmpIdVerifiedDTO;
import com.hepl.budgie.dto.preonboarding.TodayJoiningDTO;
import com.hepl.budgie.entity.preonboarding.DayZeroResponse;
import com.hepl.budgie.entity.preonboarding.ToggleFileRequest;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.UserOtherDocuments;
import com.hepl.budgie.service.preonboarding.DayZeroService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/on-boarding")
@RequiredArgsConstructor
public class DayZeroController {

    private final DayZeroService dayZeroService;
    private final Translator translator;

    @GetMapping("/dayZeroData")
    public GenericResponse<List<DayZeroResponse>> fetchData() {
        List<DayZeroResponse> dayZeroData = dayZeroService.fetchDayZeroData();

        return GenericResponse.<List<DayZeroResponse>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .data(dayZeroData)
                .build();
    }
    @GetMapping("/todayDateJoining")
    public GenericResponse<List<TodayJoiningDTO>> fetchToday() {
        List<TodayJoiningDTO> todayJoiningDTO = dayZeroService.fetchTodayDateOfJoining();
        return GenericResponse.<List<TodayJoiningDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_STATUS_FETCH))
                .errorType("NONE")
                .data(todayJoiningDTO)
                .build();
    }
    @GetMapping("/getDocuments/{empId}")
    public GenericResponse<List<DocumentDetailDTO>> getDocuments(@PathVariable String empId) {
        List<DocumentDetailDTO> documentsData = dayZeroService.getDocuments(empId);

        if (documentsData == null || documentsData.isEmpty()) {
            return GenericResponse.<List<DocumentDetailDTO>>builder()
                    .status(false)
                    .message(AppMessages.DOCUMENT_NOT_FOUND)
                    .data(Collections.emptyList())
                    .build();
        }

        return GenericResponse.<List<DocumentDetailDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DOCUMENT_FETCH))
                .errorType("NONE")
                .data(documentsData)
                .build();
    }


    @PostMapping("/toggleFile/{empId}")
    public GenericResponse<Map<String, Object>> toggleFile(
            @PathVariable String empId,
            @RequestBody ToggleFileRequest request) {
        return dayZeroService.toggleFile(empId, request.getDocument(), request.getStatus());

    }

    @PostMapping("/on-boardingStatus/{empId}")
    public GenericResponse getByEmpId(@PathVariable String empId) {
        UserInfo user = dayZeroService.getByEmpId(empId);

        if (user == null) {
            return GenericResponse.builder()
                    .status(false)
                    .message(AppMessages.EMPLOYEEID_NOT_FOUND)
                    .errorType("NOT_FOUND")
                    .data(null)
                    .build();
        }

        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.ONBOARDING_INFO))
                .errorType("NONE")
                .build();
    }

    @PostMapping("/updateVerifiedAt")
    public GenericResponse updateVerifiedAt(@RequestBody EmpIdVerifiedDTO request) {
        List<String> empIds = request.getEmpIds();
        List<UserOtherDocuments> updatedDocuments = dayZeroService.updateVerifiedAt(empIds);
        return GenericResponse.builder()
                .status(true)
                .message(AppMessages.PRE_ONBOARDING_VERIFIED)
                .errorType("NONE")                .data(AppMessages.UPDATED)
                .build();
    }



}


