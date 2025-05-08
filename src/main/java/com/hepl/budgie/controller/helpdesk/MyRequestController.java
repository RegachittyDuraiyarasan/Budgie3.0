package com.hepl.budgie.controller.helpdesk;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.helpdesk.*;
import com.hepl.budgie.service.helpdesk.MyRequestService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/myRequest")
@Slf4j
@RequiredArgsConstructor
public class MyRequestController {
    private final MyRequestService myRequestService;
    @GetMapping("/getAllPending")
    public GenericResponse<List<Map<String, Object>>> getAllPending(@RequestParam String referenceName, @RequestParam String org) {
        List<Map<String, Object>> myRequest = myRequestService.fetchMyRequest(referenceName, org);

        return myRequest.isEmpty()
                ? GenericResponse.error("NO_DATA", AppMessages.EMPLOYEES_RETRIEVED_SUCCESS)
                : GenericResponse.success(myRequest);
    }

    @GetMapping("/getAllCompleted")
    public GenericResponse<List<Map<String, Object>>> fetchMyRequestCompleted(@RequestParam String referenceName, @RequestParam String org) {
        List<Map<String, Object>> myRequest = myRequestService.fetchMyRequestCompleted(referenceName, org);

        return myRequest.isEmpty()
                ? GenericResponse.error("NO_DATA", AppMessages.EMPLOYEES_RETRIEVED_SUCCESS)
                : GenericResponse.success(myRequest);
    }
    @PostMapping("/updateRemarks/{tickId}")
    public GenericResponse<String> updateRemarks(@PathVariable String tickId,@RequestBody HelpdeskRemarksDTO helpdeskRemarksDTO ) {
        String remarks = myRequestService.updateRemarks(helpdeskRemarksDTO ,tickId);
        return remarks.isEmpty()
                ? GenericResponse.error("NO_DATA", AppMessages.EMPLOYEES_RETRIEVED_SUCCESS)
                : GenericResponse.success(remarks);
    }

    @GetMapping("/getHrHelpDeskReport")
    public GenericResponse<List<HrHelpDeskReportDTO>> getHrHelpDeskReport(
            @RequestParam String referenceName,
            @RequestParam String org,
            @ModelAttribute HelpDeskReportFilterDTO filter) {

        List<HrHelpDeskReportDTO> myRequest = myRequestService.fetchHrHelpDeskReport(referenceName, org, filter);

        return myRequest.isEmpty()
                ? GenericResponse.error("NO_DATA", AppMessages.EMPLOYEES_RETRIEVED_SUCCESS)
                : GenericResponse.success(myRequest);
    }

    @GetMapping("/getCount")
    public GenericResponse<HelpDeskMovementGraphDTO> getCount(
            @RequestParam String referenceName,
            @RequestParam String org,
            @ModelAttribute HelpDeskMovementFilterDTO filter) {

        HelpDeskMovementGraphDTO count = myRequestService.fetchCount(referenceName, org, filter);

        return (count == null)
                ? GenericResponse.error("NO_DATA", AppMessages.NO_DATA_FOUND)
                : GenericResponse.success(count);
    }

    @GetMapping("/getTicketStatus")
    public GenericResponse<HelpDeskGraphDTO> getTicketStatus(
            @RequestParam String referenceName,
            @RequestParam String org,
            @ModelAttribute HelpDeskReportFilterDTO filter) {

        HelpDeskGraphDTO count = myRequestService.fetchTicketStatus(referenceName, org, filter);

        return (count == null)
                ? GenericResponse.error("NO_DATA", AppMessages.NO_DATA_FOUND)
                : GenericResponse.success(count);
    }


}

