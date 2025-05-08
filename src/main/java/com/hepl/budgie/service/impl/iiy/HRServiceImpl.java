package com.hepl.budgie.service.impl.iiy;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.iiy.IIYDetails;
import com.hepl.budgie.repository.iiy.EmployeeRepository;
import com.hepl.budgie.service.excel.ExcelExport;
import com.hepl.budgie.service.iiy.HRService;
import com.hepl.budgie.utils.AppUtils;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class HRServiceImpl implements HRService {
    private final Map<String, ExcelExport> excelExport;
    private final EmployeeRepository employeeRepository;
    private final MongoTemplate mongoTemplate;
    private final EmployeeServiceImpl iiyEmployeeServiceImpl;
    private final CourseCategoryServiceImpl courseCategoryServiceImpl;
    private final JWTHelper jwtHelper;

    public BulkWriteResult excelImport(List<Map<String, Object>> validRows) {
        if (validRows.isEmpty()) {
            return BulkWriteResult.unacknowledged();
        }

        log.info("Adding Activity {}", validRows);
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        List<IIYDetails> iiyDetailsList = new ArrayList<>();
        String activityId = employeeRepository.findTopByOrderByIdDesc(organizationCode, mongoTemplate)
                .map(e -> AppUtils.generateUniqueId(e.getActivityId()))
                .orElse("IA000001");
        int activityCounter = Integer.parseInt(activityId.replace("IA", ""));
        for (Map<String, Object> row : validRows) {
            String employeeId = (String) row.get("Employee_ID");
            String financialYear = (String) row.get("Financial_Year");
            LocalDate iiyFrom = (LocalDate) row.get("Date");
            log.info("date from valid row", row.get("Date"));

            // Extract only the key-value pairs between "Date" and "Total_Hours"
            Map<String, Object> filteredData = extractBetweenDateAndTotalHours(row);
            log.info("Converted IIYDetails Map: {}", filteredData);

            for (Map.Entry<String, Object> categoryData : filteredData.entrySet()) {
                String categoryName = categoryData.getKey();
                String durationStr = categoryData.getValue().toString(); // Ensure it’s a String
                log.info("Category: {} - Value: {}", categoryName, durationStr);

                if (!employeeId.isEmpty() && !durationStr.isEmpty() && !categoryName.isEmpty()) {
                    log.info("Processing categoryName: {}, duration: {}", categoryName, durationStr);

                    try {
                        double decimalDuration = Double.parseDouble(durationStr);
                        if (decimalDuration != 0.0) {
                            log.info("Valid duration found: {} for category {}", decimalDuration, categoryName);

                            String formattedDuration = convertDecimalToHoursAndMinutes(decimalDuration);

                            // Create and save IIYDetails
                            IIYDetails newIiyDetails = new IIYDetails();

                            newIiyDetails.setEmpId(employeeId);
                            newIiyDetails.setActivityId(activityId);
                            newIiyDetails.setFinancialYear(financialYear);
                            newIiyDetails.setIiyDate(iiyFrom.atStartOfDay(ZoneId.systemDefault()));
                            newIiyDetails.setCourseCategory(categoryName);
                            newIiyDetails.setCourse("");
                            newIiyDetails.setDescription("");
                            newIiyDetails.setDuration(formattedDuration);
                            newIiyDetails.setRemarks("");
                            newIiyDetails.setCertification("No");
                            newIiyDetails.setRmStatus(Status.PENDING.label);
                            newIiyDetails.setRmRemarks("");
                            newIiyDetails.setFileName("");
                            newIiyDetails.setCreatedDate(LocalDateTime.now());
                            newIiyDetails.setCreatedByUser(authUser);
                            activityCounter++;
                            activityId = String.format("IA%06d", activityCounter);

                            iiyDetailsList.add(newIiyDetails);
                        }
                    } catch (NumberFormatException e) {
                        log.error("Error parsing duration for category {}: {}", categoryName, durationStr, e);
                    }
                }
            }
        }

        log.info("Final IIYDetails List: {}", iiyDetailsList);
        return employeeRepository.bulkActivityInsert(mongoTemplate, organizationCode, iiyDetailsList);
    }

    private Map<String, Object> extractBetweenDateAndTotalHours(Map<String, Object> row) {
        Map<String, Object> extractedData = new LinkedHashMap<>();
        boolean startExtracting = false;

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String key = entry.getKey();

            if ("Date".equals(key)) {
                startExtracting = true;
                continue;
            }

            if ("Total_Hours".equals(key)) {
                break;
            }

            if (startExtracting) {
                extractedData.put(key, entry.getValue());
            }
        }

        return extractedData;
    }

    public String convertDecimalToHoursAndMinutes(double decimalHours) {
        int hours = (int) decimalHours; // Get the whole number part (hours)
        int minutes = (int) ((decimalHours - hours) * 60); // Get the fractional part and convert to minutes
        return String.format("%02d:%02d", hours, minutes);
    }

    @Override
    public ResponseEntity<byte[]> importActivityLists(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Please upload a file".getBytes());
        }

        Workbook resultWorkbook = new XSSFWorkbook();
        Sheet resultSheet = resultWorkbook.createSheet("Upload Results");
        int resultRowCount = 0;
        Row resultHeaderRow = resultSheet.createRow(resultRowCount++);

        resultHeaderRow.createCell(0).setCellValue("Row");
        resultHeaderRow.createCell(1).setCellValue("Employee ID");
        resultHeaderRow.createCell(2).setCellValue("Status");

        CellStyle errorStyle = createCellStyle(resultWorkbook, IndexedColors.RED);
        CellStyle successStyle = createCellStyle(resultWorkbook, IndexedColors.GREEN);

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            XSSFSheet workSheet = workbook.getSheetAt(0);
            int rowCount = workSheet.getPhysicalNumberOfRows();

            if (rowCount <= 1) {
                return ResponseEntity.badRequest()
                        .body("Error: The uploaded file is empty or contains only headers.".getBytes());
            }
            Map<Integer, String> columnHeaders = new HashMap<>();
            Row headerRow = workSheet.getRow(0);
            for (int colIndex = 0; colIndex < headerRow.getLastCellNum(); colIndex++) {
                Cell cell = headerRow.getCell(colIndex);
                columnHeaders.put(colIndex, cell != null ? cell.getStringCellValue() : "Column " + (colIndex + 1));
            }
            Pattern financialYearPattern = Pattern.compile("^\\d{4}-\\d{4}$"); // YYYY-YYYY
            Pattern datePattern = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$"); // (dd-MM-yyyy)
            List<String> errorMessages = new ArrayList<>();

            for (int i = 1; i < rowCount; i++) {
                Row row = workSheet.getRow(i);
                if (row == null)
                    continue;
                // String employeeId = row.getCell(0) != null ?
                // row.getCell(0).getStringCellValue() : "";
                String employeeId = "";
                if (row.getCell(0) != null) {
                    if (row.getCell(0).getCellType() == CellType.NUMERIC) {
                        employeeId = String.format("%.0f", row.getCell(0).getNumericCellValue());// Convert numeric to
                                                                                                 // string
                    } else if (row.getCell(0).getCellType() == CellType.STRING) {
                        employeeId = row.getCell(0).getStringCellValue().trim(); // Get string value
                    }
                }
                log.info("employeeId", employeeId);

                boolean isValid = true;
                // Validate Employee ID
                String empId = getCellValue(row, 0);

                if (empId.isEmpty()) {
                    isValid = false;
                    errorMessages.add("Employee ID is required. ");
                }
                // Validate Financial Year
                String financialYear = getCellValue(row, 2);

                if (financialYear.isEmpty()) {
                    isValid = false;
                    errorMessages.add("Financial Year is required. ");
                } else if (!financialYearPattern.matcher(financialYear).matches()) {
                    isValid = false;
                    errorMessages.add("Financial Year format must be YYYY-YYYY (e.g., 2024-2025). ");
                }
                String dateValue = getCellValue(row, 7);

                if (dateValue.isEmpty()) {
                    isValid = false;
                    errorMessages.add("Date is required. ");
                } else if (!datePattern.matcher(dateValue).matches()) {
                    isValid = false;
                    errorMessages.add("Date format must be dd-MM-yyyy (e.g., 02-12-2024). ");
                }
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null)
                        continue;

                    try {
                        // Attempt to get the cell value as String
                        String cellValue = getCellValue(row, j);
                        log.info("Row: {}, Column: {}, Value: {}, Cell Type: {}", i + 1, j + 1, cellValue,
                                cell.getCellType());
                    } catch (IllegalStateException e) {
                        log.error("Error at Row: {}, Column: {}. Message: {}", i + 1, j + 1, e.getMessage());
                    }
                }
                // Null validation for dynamic columns
                // for (int j = 8; j < row.getLastCellNum(); j++) { // start of Dynamic column
                // if ("Total Hours".equals(row.getCell(j)))
                // break; // end at "Total Hours" column
                // Cell cell = row.getCell(j);
                // if (cell == null || cell.getCellType() == CellType.BLANK) {
                // isValid = false;
                // String columnName = columnHeaders.getOrDefault(j, "Column " + (j + 1));
                // errorMessages.add(columnName + " Column is empty, Enter a default value of 0.
                // ");
                // }
                // }
                for (String errorMessage : errorMessages) {
                    // Add results to the result sheet
                    Row resultRow = resultSheet.createRow(resultRowCount++);
                    resultRow.createCell(0).setCellValue("Row " + (i + 1));
                    resultRow.createCell(1).setCellValue(employeeId);
                    resultRow.createCell(2).setCellValue(isValid ? "Valid Data" : errorMessage.toString());
                    resultRow.getCell(2).setCellStyle(isValid ? successStyle : errorStyle);
                }

            }
            if (!errorMessages.isEmpty()) {
                return generateErrorFile(resultWorkbook);
            }

            boolean startIndexing = false;
            Map<Integer, String> columnMap = new HashMap<>();
            List<Integer> index = new ArrayList<>();

            for (int i = workSheet.getFirstRowNum(); i <= workSheet.getLastRowNum(); i++) {
                Row row = workSheet.getRow(i);
                if (row == null)
                    continue;

                // Map to store data for the current row
                Map<Integer, Map<String, String>> objectsMap = new HashMap<>();

                // Process headers in the first row
                if (i == 0) {
                    for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
                        Cell cell = row.getCell(j);
                        String header = cell != null ? cell.getStringCellValue() : "";
                        if ("Employee Id".equals(header) || "Employee Name".equals(header) ||
                                "Financial Year".equals(header) || "Date".equals(header)) {
                            columnMap.put(j, header);
                            index.add(j);
                        }
                        if ("Date".equals(header.trim())) {
                            startIndexing = true;
                        } else if ("Total Hours".equals(header)) {
                            startIndexing = false;
                        } else if (startIndexing) {
                            columnMap.put(j, header);
                            index.add(j);
                        }
                    }
                    continue;
                }

                // Extract and process data from each row
                for (int j : index) {
                    Cell cell = row.getCell(j);
                    splitDynamicMap(cell, objectsMap, i, columnMap.get(j), columnMap.get(j).equals("Employee Id"));
                }
                log.info("Row " + i + " - objectsMap: " + objectsMap);

                List<Map<String, String>> extractedList = new ArrayList<>();
                for (Map.Entry<Integer, Map<String, String>> entry : objectsMap.entrySet()) {
                    Map<String, String> rowMap = entry.getValue();
                    String employeeId = rowMap.getOrDefault("Employee Id", "");
                    String employeeName = rowMap.getOrDefault("Employee Name", "");
                    String financialYear = rowMap.getOrDefault("Financial Year", "");
                    String date = rowMap.getOrDefault("Date", "");

                    log.info("Employee Id: " + employeeId + ", Employee Name: " + employeeName + ", Financial Year: "
                            + financialYear + ", Date: " + date);

                    // Prepare map with remaining data for this row
                    Map<String, String> newMap = new HashMap<>(rowMap);
                    newMap.remove("Employee Id");
                    newMap.remove("Employee Name");
                    newMap.remove("Financial Year");
                    newMap.remove("Date");
                    extractedList.add(newMap);

                    int successfulUpdatesCount = 0;
                    for (Map.Entry<String, String> categoryData : newMap.entrySet()) {
                        String categoryName = categoryData.getKey();
                        String duration = categoryData.getValue();

                        if (!employeeId.isEmpty() && !duration.isEmpty() && !categoryName.isEmpty()) {
                            log.info("categoryName: " + categoryName + ", duration: " + duration);

                            double decimalDuration = Double.parseDouble(duration);
                            if (decimalDuration != 0.0) {
                                log.info("categoryName duration if: " + categoryName + ", duration: " + duration);

                                String formattedDuration = convertDecimalToHoursAndMinutes(decimalDuration);

                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                                LocalDateTime iiyFrom = date != null
                                        ? LocalDateTime.parse(date + " 00:00:00", formatter)
                                        : null;
                                log.info("decimalDuration: " + decimalDuration);
                                log.info("formattedDuration: " + formattedDuration);

                                // Create and save IiyDetails
                                IIYDetails newIiyDetails = new IIYDetails();
                                newIiyDetails.setEmpId(employeeId);
                                newIiyDetails.setFinancialYear(financialYear);
                                // if (iiyFrom != null) {
                                // Date iiyFromAsDate =
                                // Date.from(iiyFrom.atZone(ZoneId.systemDefault()).toInstant());
                                // newIiyDetails.setIiyDate(iiyFromAsDate);
                                // }
                                newIiyDetails.setCourseCategory(categoryName);
                                newIiyDetails.setCourse("");
                                newIiyDetails.setDescription("");
                                newIiyDetails.setDuration(formattedDuration);
                                newIiyDetails.setRemarks("");
                                newIiyDetails.setCertification("No");
                                newIiyDetails.setRmStatus(Status.PENDING.label);
                                newIiyDetails.setRmRemarks("");
                                newIiyDetails.setFileName("");

                                employeeRepository.save(newIiyDetails);
                                successfulUpdatesCount++;
                            } else {
                                log.info("categoryName duration else: " + categoryName + ", duration: " + duration);

                            }

                        }
                    }

                    Row resultRow = resultSheet.createRow(resultRowCount++);
                    resultRow.createCell(0).setCellValue("Row " + (i + 1));
                    resultRow.createCell(1).setCellValue(employeeId);
                    resultRow.createCell(2)
                            .setCellValue(successfulUpdatesCount > 0 ? "Uploaded Successfully" : "Failed to Upload");
                    resultRow.getCell(2).setCellStyle(successfulUpdatesCount > 0 ? successStyle : errorStyle);
                }
            }

            return generateResultFile(resultWorkbook);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file".getBytes());
        }
    }

    private CellStyle createCellStyle(Workbook workbook, IndexedColors color) {
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(color.getIndex());
        cellStyle.setFont(font);
        return cellStyle;
    }

    // private String getCellValue(Row row, int cellIndex) {
    // Cell cell = row.getCell(cellIndex);
    // return (cell != null && cell.getCellType() == CellType.STRING) ?
    // cell.getStringCellValue() : "";
    // }
    private String getCellValue(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null)
            return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // if (DateUtil.isCellDateFormatted(cell)) {
                // SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                // return dateFormat.format(cell.getDateCellValue());
                // }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue(); // Attempt to retrieve formula result as a string
                } catch (IllegalStateException ex) {
                    return String.valueOf(cell.getNumericCellValue()); // Fall back to numeric if needed
                }
            case BLANK:
                return "";
            default:
                return "Unknown Type";
        }
    }

    private ResponseEntity<byte[]> generateErrorFile(Workbook resultWorkbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resultWorkbook.write(outputStream);
        byte[] resultBytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=upload_errors.xlsx")
                .body(resultBytes);
    }

    private ResponseEntity<byte[]> generateResultFile(Workbook resultWorkbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resultWorkbook.write(outputStream);
        byte[] resultBytes = outputStream.toByteArray();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=upload_results.xlsx")
                .body(resultBytes);
    }

    private void splitDynamicMap(Cell cell, Map<Integer, Map<String, String>> objectsMap, int i, String key,
            boolean forceString) {
        String cellValue = "";
        if (cell.getCellType() == CellType.NUMERIC) {
            if (forceString) {
                cellValue = String.format("%.0f", cell.getNumericCellValue());

            } else {
                cellValue = String.valueOf(cell.getNumericCellValue());

            }

        } else {
            cellValue = cell.getStringCellValue();
        }
        if (objectsMap.get(i) != null) {
            log.info("keyKey: " + key + "cellValue: " + cellValue);

            objectsMap.get(i).put(key, cellValue);
        } else {
            Map<String, String> columnMap = new HashMap<>();
            columnMap.put(key, cellValue);
            log.info("key: " + key + "cellValue: " + cellValue);

            objectsMap.put(i, columnMap);
        }
    }

}
