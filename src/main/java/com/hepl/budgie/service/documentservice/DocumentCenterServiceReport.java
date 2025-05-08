package com.hepl.budgie.service.documentservice;

import java.util.List;

import com.hepl.budgie.dto.documentInfo.DocumentCenterResponseReportDTo;
import com.hepl.budgie.dto.documentInfo.DocumentResponseReportDTO;

public interface DocumentCenterServiceReport {

    void addDocumentCenterReport(DocumentCenterResponseReportDTo documentcenterReport);

    List<DocumentResponseReportDTO> getDocumentReport();
}
