package com.hepl.budgie.service.helpdesk;

import com.hepl.budgie.dto.helpdesk.HelpdeskTickDTO;
import com.hepl.budgie.entity.helpdesk.HelpdeskTicketDetails;

import java.util.List;
import java.util.Map;

public interface HelpdeskTicketDetailsService {
    HelpdeskTicketDetails addHelpdeskTicket(HelpdeskTickDTO helpdeskTickDTO,String empId);
    List<Map<String, String>> getAllCategoryIds(String referenceName, String org);
}
