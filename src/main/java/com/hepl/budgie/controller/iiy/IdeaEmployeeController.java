package com.hepl.budgie.controller.iiy;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.iiy.*;

import com.hepl.budgie.service.iiy.IdeaEmployeeService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Create and ManageI Idea Activity", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/idea")
@RequiredArgsConstructor
public class IdeaEmployeeController {
    private final IdeaEmployeeService ideaEmployeeService;
    private final Translator translator;

    @PostMapping()
    @Operation(summary = "Add Idea")
    public GenericResponse<String> addIdea(@Valid @RequestBody IdeaEmployeeRequestDTO data) {
        log.info("Add Idea - {}", data);
        ideaEmployeeService.addIdea(data);
        return GenericResponse.success(translator.toLocale(AppMessages.ADDED_IDEA));
    }

    @PostMapping("/list")
    @Operation(summary = "Fetch Idea list")
    public GenericResponse<List<IdeaFetchDTO>> fetchIdeaList(@RequestBody IdeaEmployeeRequestDTO data) {
        log.info("Fetch Idea List - {}", data);
        List<IdeaFetchDTO> result = ideaEmployeeService.fetchIdeaList(data);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_IDEA), result);

    }

    @PostMapping("/team/list")
    public GenericResponse<List<IdeaFetchDTO>> fetchTeamIdeaList(@RequestBody IdeaEmployeeRequestDTO data) {
        log.info("Fetch Team Idea List - {}", data);

        List<IdeaFetchDTO> result = ideaEmployeeService.fetchTeamIdeaList(data);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_TEAM_IDEA), result);

    }

    @PostMapping("/team/report")
    public GenericResponse<List<IdeaFetchDTO>> fetchTeamIdeaReportList(@RequestBody IdeaEmployeeRequestDTO data) {
        log.info("Fetch Team Idea List - {}", data);
        List<IdeaFetchDTO> result = ideaEmployeeService.fetchTeamIdeaReportList(data);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_TEAM_IDEA_REPORT), result);

    }

    @PostMapping("/approveTeamIdea")
    public GenericResponse<String> approveTeamIdea(@RequestBody IdeaEmployeeRequestDTO data) {
        log.info("Approve Idea - {}", data);
        ideaEmployeeService.approveTeamIdea(data);
        return GenericResponse.success(translator.toLocale(AppMessages.APPROVE_IDEA));
    }

    @PostMapping("/rejectTeamIdea")
    public GenericResponse<String> rejectTeamIdea(@RequestBody IdeaEmployeeRequestDTO data) {
        log.info("Reject Idea - {}", data);
        ideaEmployeeService.rejectTeamIdea(data);
        return GenericResponse.success(translator.toLocale(AppMessages.REJECT_IDEA));
    }

    @PostMapping("/overAllIdeaReportList")
    public Object fetchOverAllIdeaReportList(@RequestBody IdeaEmployeeRequestDTO data) {
        log.info("Fetch Over All Idea List - {}", data);
        List<IdeaFetchDTO> result = ideaEmployeeService.fetchTeamIdeaReportList(data);
        return GenericResponse.success(translator.toLocale(AppMessages.FETCH_OVERALL_IDEA_REPORT), result);

    }

}
