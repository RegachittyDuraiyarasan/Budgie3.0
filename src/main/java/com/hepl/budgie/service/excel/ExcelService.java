package com.hepl.budgie.service.excel;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.excel.ValidationResult;
import com.mongodb.bulk.BulkWriteResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface ExcelService {
    byte[] sampleExcel(String sampleFile) throws IOException;

    ValidationResult excelImport(String sampleFile, String validationFile, MultipartFile file) throws IOException, InterruptedException, ExecutionException;
    byte[] responseExcel(ValidationResult validationResult, BulkWriteResult dbResult, String customColumn) throws IOException;

    byte[] existingRecords(List<Map<String, Object>> data) throws IOException;
    byte[] payrollResponseExcel(List<HeaderList> headerList, List<List<String>> data) throws IOException;

    byte[] generateSuccessFile(String locale);

    byte[] generateErrorFile(Map<String, List<String>> response) throws IOException;


}
