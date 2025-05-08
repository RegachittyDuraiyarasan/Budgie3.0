package com.hepl.budgie.controller.documentcenter;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.documentInfo.DocumentCenterResponseReportDTo;
import com.hepl.budgie.dto.documentInfo.DocumentResponseReportDTO;
import com.hepl.budgie.service.documentservice.DocumentCenterServiceReport;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/document-Report")
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentCenterReportController {
    private final Translator translator;
    private final DocumentCenterServiceReport documentCenterServiceReport;

    @PostMapping()
    public GenericResponse<String> addDocumentCenterReport(
            @Valid @RequestBody DocumentCenterResponseReportDTo documentCenterResponseReportDTo) {
        documentCenterServiceReport.addDocumentCenterReport(documentCenterResponseReportDTo);
        return GenericResponse.success(translator.toLocale(AppMessages.DOCUMENT_REPORT_ADDED));
    }

    @GetMapping()
    public GenericResponse<List<DocumentResponseReportDTO>> getDocumentCenterReport() {
        return GenericResponse.success(documentCenterServiceReport.getDocumentReport());
    }

}
