package com.hepl.budgie.service.helpdesk;

import com.hepl.budgie.dto.helpdesk.*;

import java.util.List;
import java.util.Map;

public interface MyRequestService {
    List<Map<String, Object>> fetchMyRequest(String referenceName, String org);
    List<Map<String, Object>> fetchMyRequestCompleted(String referenceName, String org);
    String updateRemarks(HelpdeskRemarksDTO helpdeskRemarksDTO ,String tickId);

   List<HrHelpDeskReportDTO>fetchHrHelpDeskReport(String referenceName, String org, HelpDeskReportFilterDTO filter);
    HelpDeskMovementGraphDTO fetchCount(String referenceName, String org, HelpDeskMovementFilterDTO filter);
    HelpDeskGraphDTO fetchTicketStatus(String referenceName, String org, HelpDeskReportFilterDTO filter);
}
