package com.hepl.budgie.controller.helpdesk;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.helpdesk.HelpdeskTickDTO;
import com.hepl.budgie.entity.helpdesk.HelpdeskTicketDetails;
import com.hepl.budgie.service.helpdesk.HelpdeskTicketDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/helpdeskTicket")
@RequiredArgsConstructor
public class HelpdeskTicketDetailsController {
    private final HelpdeskTicketDetailsService helpdeskTicketDetailsService;

    @PostMapping(value = "/create/{empId}", consumes = "multipart/form-data")
    public GenericResponse<HelpdeskTicketDetails> createTicket(
            @PathVariable("empId") String empId,
            @RequestParam("ticketRaisedAt") String ticketRaisedAt,
            @RequestParam("category") String category,
            @RequestParam("details") String details,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "employeeCode", required = false) String employeeCode
    ) {
        HelpdeskTickDTO helpdeskTickDTO = new HelpdeskTickDTO();
        helpdeskTickDTO.setTicketRaisedAt(ticketRaisedAt);
        helpdeskTickDTO.setCategory(category);
        helpdeskTickDTO.setDetails(details);
        helpdeskTickDTO.setFile(file);
        helpdeskTickDTO.setUserName(userName);
        helpdeskTickDTO.setEmployeeCode(employeeCode);
        HelpdeskTicketDetails ticket = helpdeskTicketDetailsService.addHelpdeskTicket(helpdeskTickDTO,empId);

        return GenericResponse.<HelpdeskTicketDetails>builder()
                .status(true)
                .message("Ticket created successfully")
                .data(ticket)
                .errorType("NONE")
                .build();
    }
    @GetMapping("/all")
    public GenericResponse<List<Map<String, String>>> getAll(
            @RequestParam("referenceName") String referenceName,
            @RequestParam("org") String org) {

        List<Map<String, String>> categories = helpdeskTicketDetailsService.getAllCategoryIds(referenceName, org);

        return categories.isEmpty()
                ? GenericResponse.error("NO_DATA", "No categories found.")
                : GenericResponse.success(categories);
    }


}
