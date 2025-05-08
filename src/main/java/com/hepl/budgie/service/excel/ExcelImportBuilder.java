package com.hepl.budgie.service.excel;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.exceptions.FieldException;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public class ExcelImportBuilder {
    private final MultipartFile file;
    private final boolean validateHeaders;
    private final List<HeaderList> requiredHeaders;

    public ExcelImportBuilder(Builder builder) {
        this.file = builder.file;
        this.validateHeaders = builder.validateHeaders;
        this.requiredHeaders = builder.requiredHeaders;
    }
    public static class Builder {
        private MultipartFile file;
        private boolean validateHeaders = false;
        private List<HeaderList> requiredHeaders = new ArrayList<>();

        public Builder setFile(MultipartFile file) {
            this.file = file;
            return this;
        }

        public Builder validateHeaders(boolean validate) {
            this.validateHeaders = validate;
            return this;
        }

        public Builder setRequiredHeaders(List<HeaderList> requiredHeaders) {
            this.requiredHeaders = requiredHeaders;
            return this;
        }

        public ExcelImportBuilder build() {
            return new ExcelImportBuilder(this);
        }
    }
    public List<Map<String, Object>> importData() throws IOException, InterruptedException , ExecutionException {
        try (Workbook workbook =new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EMPTY_EXCEl);
            }
            //Headers
            Row headerRow = sheet.getRow(0);
            List<String> headers = extractHeaders(headerRow);
            log.info("Request Body Excel Headers -{}", headers);
            //Validate Headers
            if(validateHeaders) {
                headerValidation(headers);
            }

            // Check if there are data rows
            if (sheet.getLastRowNum() < 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EMPTY_EXCEl);
            }

            List<Map<String, Object>> allRows = new ArrayList<>();
            ExecutorService executor = Executors.newCachedThreadPool();

            // Process data rows
            List<Future<List<Map<String, Object>>>> futures = new ArrayList<>();
            int maxBatchSize = 10;
            List<Row> batchRows = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                batchRows.add(row);

                // Check if batch should be submitted
                if (shouldSubmitBatch(batchRows.size(), i, sheet.getLastRowNum(), maxBatchSize)) {
                    final List<Row> finalBatch = new ArrayList<>(batchRows);
                    futures.add(executor.submit(() -> processBatch(finalBatch, headers)));
                    batchRows.clear(); // Clear after submission
                }
            }

            // Collect results
            for (Future<List<Map<String, Object>>> future : futures) {
                allRows.addAll(future.get());  // Blocking call, waits for task to finish
            }
            executor.shutdown();
            return allRows;

        }
    }
    private List<String> extractHeaders(Row headerRow) {
        return IntStream.range(headerRow.getFirstCellNum(), headerRow.getLastCellNum())
                .mapToObj(i -> headerRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
                .map(Cell::getStringCellValue)
                .map(header -> header.trim().replaceAll("\\s+", "_")).collect(Collectors.toList());

    }
    private void headerValidation(List<String> headers) {
        Set<String> headerSet = requiredHeaders.stream()
                .map(header -> header.getHeader().trim())
                .collect(Collectors.toSet());
        List<String> invalidHeaders = headers.stream()
                .filter(header -> !headerSet.contains(header.trim()))
                .toList();
        log.info("Result :{}", invalidHeaders );

        if(!invalidHeaders.isEmpty()){
            throw new CustomResponseStatusException(AppMessages.INVALID_HEADERS, HttpStatus.NOT_FOUND, new Object[]{String.join(",", invalidHeaders)});
//            throw  new ResponseStatusException(HttpStatus.NOT_FOUND, String.join(",", invalidHeaders) +" is not found");
        }


    }
    private List<Map<String, Object>> processBatch(List<Row> batch, List<String> headers) {
        log.info("Processing batch of size: {}", batch.size());
        return batch.stream()
                .map(row -> processRow(row, headers))
                .collect(Collectors.toList());
    }
    private Map<String, Object> processRow(Row row, List<String> headers) {
        Map<String, Object> rowData = new LinkedHashMap<>();
        rowData.put("row", "Row " + row.getRowNum());
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            int finalI = i;
            String type = requiredHeaders.stream()
                    .filter(headerFil -> headerFil.getHeader().trim().equalsIgnoreCase(headers.get(finalI).trim()))
                    .map(HeaderList::getDataType)
                    .findFirst().orElse("String");
            rowData.put(headers.get(i), getCellValue(cell, type));
        }
        return rowData;
    }

    private boolean shouldSubmitBatch(int currentBatchSize, int currentRowIndex, int lastRowIndex, int maxBatchSize) {
        return currentBatchSize >= maxBatchSize || currentRowIndex == lastRowIndex;
    }

    private Object getCellValue(Cell cell, String type) {
        return switch (type.toLowerCase()) {
            case "string" -> {
                if (cell.getCellType() == CellType.STRING) {
                    yield cell.getStringCellValue().trim();
                } else if (cell.getCellType() == CellType.NUMERIC) {
                    yield String.valueOf((long) cell.getNumericCellValue()); // Convert to String without decimal
                } else {
                    yield "";
                }
            }
            case "date" -> DateUtil.isCellDateFormatted(cell) ? cell.getLocalDateTimeCellValue().toLocalDate() : cell.getNumericCellValue();
            case "boolean" -> cell.getBooleanCellValue();
            case "int" -> (int) Math.round(cell.getNumericCellValue());
            default -> cell.toString();
        };
    }


}
