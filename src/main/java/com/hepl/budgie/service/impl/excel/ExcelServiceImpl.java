package com.hepl.budgie.service.impl.excel;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.hepl.budgie.service.excel.*;
import com.hepl.budgie.service.impl.excelValidation.ExcelValidationService;
import com.hepl.budgie.service.impl.excelValidation.ValidationErrorTransformer;
import com.hepl.budgie.utils.BulkUpsertUtil;
import com.mongodb.bulk.BulkWriteResult;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ExcelServiceImpl implements ExcelService {
    private final Map<String, ExcelExport> excelExport;
    private final ExcelValidationService excelValidationService;
    private final BulkUpsertUtil bulkUpsertUtil;

    public ExcelServiceImpl(List<ExcelExport> strategies, ExcelValidationService excelValidationService, BulkUpsertUtil bulkUpsertUtil) {
        this.excelExport = strategies.stream()
                .collect(Collectors.toMap(strategy -> strategy.getClass().getSimpleName(), Function.identity()));
        this.excelValidationService =  excelValidationService;
        this.bulkUpsertUtil =  bulkUpsertUtil;
    }

    public byte[] sampleExcel(String sampleFile) throws IOException{
        ExcelExport strategy = excelExport.getOrDefault(sampleFile, null);
        List<HeaderList>  headers =  strategy.prepareHeaders();
        List<ExcelBuilder.DropdownConfig>  dropdowns =  strategy.prepareDropdowns();
        List<List<String>>  dataRows =  strategy.prepareDataRows();

        log.info("Excel Headers: {}", headers);
        ExcelBuilder excelBuilder = new ExcelBuilder.Builder()
                .setHeaders(headers)
                .setDropdowns(dropdowns)
                .setDataRows(dataRows)
                .build();

        return excelBuilder.buildExcel();

    }
    public ValidationResult excelImport(String sampleFile, String validationFile, MultipartFile file) throws IOException, InterruptedException, ExecutionException {
        ExcelExport strategy = excelExport.getOrDefault(sampleFile, null);
        List<HeaderList> headers = strategy.prepareHeaders();
        log.info("Excel Headers: {}", headers);

        ExcelImportBuilder importBuilder = new ExcelImportBuilder.Builder()
                .setFile(file)
                .validateHeaders(true)
                .setRequiredHeaders(headers) 
                .build();

        List<Map<String, Object>> data = importBuilder.importData();
        log.info("Row Data: {}", data);

        ValidationResult validationResult = excelValidationService.validateExcelData(data, headers, validationFile);
        log.info("After Validation Valid Rows-{}", validationResult.getValidRows());
        log.info("After Validation InValid Rows-{}", validationResult.getInvalidRows());

        return validationResult;


    }
    public byte[] responseExcel(ValidationResult validationResult, BulkWriteResult dbResult, String customColumn) throws IOException{

        List<Map<String, String>> validationErrors = ValidationErrorTransformer.transformValidationErrors(validationResult.getInvalidRows(), customColumn);
        List<Map<String, String>> dbMsg =  new ArrayList<>();
        if(dbResult.wasAcknowledged()) {
            dbMsg.addAll(bulkUpsertUtil.processBulkUpsertResults(dbResult.getUpserts(), validationResult.getValidRows(), customColumn));
        }

        List<List<String>> dataRows = Stream.concat(validationErrors.stream(), dbMsg.stream())
                .map(map -> map.values().stream().map(String::valueOf).collect(Collectors.toList())) // Convert to List<String>
                .collect(Collectors.toList());

        log.info("DB Msg-{}", dataRows);


        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Row", false, ""),
                new HeaderList(customColumn, false, ""),
                new HeaderList("Column", false, ""),
                new HeaderList("Message", false, "")
        ).toList());

        ExcelBuilder excelBuilder = new ExcelBuilder.Builder()
                .setHeaders(headerList)
                .setDataRows(dataRows)
                .build();

        return excelBuilder.buildExcel();
    }

    public byte[] existingRecords(List<Map<String, Object>> data) throws IOException {

        List<HeaderList> headerList = new ArrayList<>();
        List<List<String>> dataRows = new ArrayList<>();


        ExcelBuilder excelBuilder = new ExcelBuilder.Builder()
                .setHeaders(headerList)
                .setDataRows(dataRows)
                .build();

        return excelBuilder.buildExcel();
    }

    public byte[] payrollResponseExcel(List<HeaderList> headerList, List<List<String>> data) throws IOException {

        ExcelBuilder excelBuilder = new ExcelBuilder.Builder()
                .setHeaders(headerList)
                .setDataRows(data)
                .build();

        return excelBuilder.buildExcel();
    }

    public byte[] generateSuccessFile(String message) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Upload Report");
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellValue(message);

            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                workbook.write(outputStream);
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate success file", e);
        }
    }

    @Override
    public byte[] generateErrorFile(Map<String, List<String>> errors) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Errors");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Row Number");
        headerRow.createCell(1).setCellValue("Error Message");

        int rowIndex = 1;
        for (Map.Entry<String, List<String>> entry : errors.entrySet()) {
            String rowNumber = entry.getKey();
            for (String errorMessage : entry.getValue()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(rowNumber);
                row.createCell(1).setCellValue(errorMessage);
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        }
    }


}
