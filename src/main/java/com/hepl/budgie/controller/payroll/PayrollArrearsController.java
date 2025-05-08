package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.payroll.*;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.service.payroll.PayrollArrearsService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payroll/arrears")
public class PayrollArrearsController {
    private final PayrollArrearsService payrollArrearsService;
    private final ExcelService excelService;
    @PostMapping()
    @Operation(summary = "Process New Joined Employee Arrears")
    public GenericResponse<List<PayrollCTCBreakupsDTO>> add() {
        return GenericResponse.success(payrollArrearsService.newJoinerArrears());
    }

    @PostMapping("/employeeArrears")
    @Operation(summary = "Pay Arrears for Revision CTC")
    public ResponseEntity<byte[]> processArrears(@RequestBody @Valid PayrollArrearsEmpDTO dto) throws IOException {

        List<List<String>> data = payrollArrearsService.processExistingEmpArrears(dto.getEmpId(), dto.getWithEffectDate());
        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String"),
                new HeaderList("Message", true, "String")
        ).toList());
        byte[] excelContent = excelService.payrollResponseExcel(headerList, data);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "Arrears_Validations.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }

    @GetMapping()
    @Operation(summary = "Employee Arrears List")
    public GenericResponse<List<Map<String,Object>>> list() {
        return GenericResponse.success(payrollArrearsService.arrearsList(""));
    }


}
