package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.service.payroll.PayrollYTDReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payroll/ytd")
@Tag(name = "Payroll YTD Reports", description = "Year-to-Date projection data for employees")
@Slf4j
@RequiredArgsConstructor
public class PayrollYTDReportController {
    private final PayrollYTDReportService ytdReportService;

    @GetMapping()
    public GenericResponse<List<String>> fetchYtdData(){
        return GenericResponse.success(ytdReportService.list());
    }

}
