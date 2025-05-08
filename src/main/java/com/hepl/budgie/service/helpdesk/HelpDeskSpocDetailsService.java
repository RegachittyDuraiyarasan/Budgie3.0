package com.hepl.budgie.service.helpdesk;


import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.helpdesk.EmployeeDto;
import com.hepl.budgie.entity.helpdesk.HelpDeskSPOCDetails;
import reactor.core.publisher.Mono;

import java.util.List;

public interface HelpDeskSpocDetailsService {

    void addSPOCDetails(HelpDeskSPOCDetails helpDeskSPOCDetails, String org);

    Mono<GenericResponse<List<EmployeeDto>>> getEmployeeDetails();
}
