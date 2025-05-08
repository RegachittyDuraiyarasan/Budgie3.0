package com.hepl.budgie.controller.helpdesk;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.helpdesk.EmployeeDto;
import com.hepl.budgie.entity.helpdesk.HelpDeskSPOCDetails;
import com.hepl.budgie.service.helpdesk.HelpDeskSpocDetailsService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/helpdesk")
@RequiredArgsConstructor
public class HelpdeskSpocDetailsController {

    private final HelpDeskSpocDetailsService helpDeskSpocDetailsService;

    private final Translator translator;

    @PostMapping()
    public GenericResponse<String> addSPOCDetails(@RequestBody HelpDeskSPOCDetails helpDeskSPOCDetails, String org){
        helpDeskSpocDetailsService.addSPOCDetails(helpDeskSPOCDetails,org);
        return GenericResponse.success(translator.toLocale(AppMessages.SPOC_ADDED));
    }

    @GetMapping()
    public Mono<GenericResponse<List<EmployeeDto>>> getEmployeeList() {
        return helpDeskSpocDetailsService.getEmployeeDetails();
    }




}
