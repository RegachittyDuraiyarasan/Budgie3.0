package com.hepl.budgie.controller.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.leavemanagement.LeaveScheme;
import com.hepl.budgie.service.leavemanagement.LeaveSchemeService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Leave Scheme", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/leave/scheme")
@Slf4j
public class LeaveSchemeCtrl {
	
    private final LeaveSchemeService leaveSchemeService;
    private final Translator translator;
    private final MasterFormService masterFormService;


    @GetMapping()
    @Operation(summary = "Get Active Leave Scheme")
    public GenericResponse<List<LeaveScheme>> fetch() {
        log.info("Get Leave Scheme List");
        return GenericResponse.success(leaveSchemeService.getLeaveSchemeList());
    }

    @GetMapping("/list")
    @Operation(summary = "Get Active Leave Scheme Names")
    public GenericResponse<List<String>> fetchSchemeNames() {
        log.info("Get Leave Scheme Names List");
        return GenericResponse.success(leaveSchemeService.getLeaveSchemeNames());
    }
    
    @PostMapping()
    @Operation(summary = "Create or Update Leave Scheme")
    public GenericResponse<String> saveForm(@RequestBody FormRequest formRequest) {
        log.info("Request received to add Leave Scheme for form: {}", formRequest.getFormName());
        try {
//            masterFormService.formValidate(formRequest,org);
            String success = leaveSchemeService.saveForm(formRequest);
            return GenericResponse.success(success);
        } catch (Exception e) {
            log.error("Error adding basic details: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public GenericResponse<String> deleteOrganizationMap(@PathVariable String id) {
        log.info("Delete Leave Scheme map {}", id);

        leaveSchemeService.deleteLeaveScheme(id);
        return GenericResponse
                .success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "Leave Scheme" }));
    }
}
