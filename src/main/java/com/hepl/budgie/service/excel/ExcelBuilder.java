package com.hepl.budgie.service.excel;


import com.hepl.budgie.dto.excel.HeaderList;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExcelBuilder {
    private final List<HeaderList> headers;
    private final List<List<String>> dataRows;
    private final List<DropdownConfig> dropdowns;
    private static final String INVALID_INPUT = "Invalid Input Detected";

    private ExcelBuilder(Builder builder) {
        this.headers = builder.headers;
        this.dataRows = builder.dataRows != null ? builder.dataRows : List.of();
        this.dropdowns = builder.dropdowns != null ? builder.dropdowns : List.of();
    }

    public static class Builder {
        private List<HeaderList> headers;
        private List<List<String>> dataRows = new ArrayList<>();
        private List<DropdownConfig> dropdowns = new ArrayList<>();

        public Builder setHeaders(List<HeaderList> headers) {
            this.headers = headers;
            return this;
        }

        public Builder setDataRows(List<List<String>> dataRows) {
            if (dataRows != null) {
                this.dataRows = dataRows;
            }
            return this;
        }

        public Builder setDropdowns(List<DropdownConfig> dropdowns) {
            if(dropdowns != null) {
                this.dropdowns = dropdowns;
            }
            return this;
        }

        public ExcelBuilder build() {
            return new ExcelBuilder(this);
        }
    }
    public static class DropdownConfig {
        int columnIndex;
        List<String> options;
        boolean isDateValidation;
        //Need to work on Alpha numeric validation
        boolean isAlphanumericValidation;
        boolean isIntegerValidation;
        boolean onlyPositive;
        String startDate;
        String endDate;

        public DropdownConfig(int columnIndex, List<String> options) {
            this.columnIndex = columnIndex;
            this.options = options;
            this.isDateValidation = false;
            this.isAlphanumericValidation = false;
            this.isIntegerValidation = false;
        }
        // Constructor for Date Validation
        public DropdownConfig(int columnIndex, String startDate, String endDate) {
            this.columnIndex = columnIndex;
            this.isDateValidation = true;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        // Constructor for Alphanumeric Validation
        public DropdownConfig(int columnIndex, boolean isAlphanumericValidation) {
            this.columnIndex = columnIndex;
            this.isAlphanumericValidation = isAlphanumericValidation;
        }

        // Constructor for Integer Validation (Supports only positive or both)
        public DropdownConfig(int columnIndex, boolean isIntegerValidation, boolean onlyPositive) {
            this.columnIndex = columnIndex;
            this.isIntegerValidation = isIntegerValidation;
            this.onlyPositive = onlyPositive;
        }
    }
    public byte[] buildExcel() throws IOException {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Sample Sheet");

            // Create Headers
            Row headerRow = sheet.createRow(0);
            int colIndex = 0;
            for (HeaderList entry : headers) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(entry.getHeader().replaceAll("_", " "));

                //Font Style & Color
                Font font = workbook.createFont();
                font.setBold(true);
                if(entry.isMandatory()){
                    font.setColor(IndexedColors.RED.getIndex());
                }
                CellStyle cellStyle = workbook.createCellStyle();
                cellStyle.setFont(font);

                cell.setCellStyle(cellStyle);
            }

            // Fill Data
            if(!dataRows.isEmpty()) {
                for (int i = 0; i < dataRows.size(); i++) {
                    Row row = sheet.createRow(i + 1);
                    for (int j = 0; j < dataRows.get(i).size(); j++) {
                        row.createCell(j).setCellValue(dataRows.get(i).get(j));
                    }
                }
            }

            // Apply Dropdowns
            if(!dropdowns.isEmpty()) {
                for (DropdownConfig dropdown : dropdowns) {
                    DataValidationHelper validationHelper = sheet.getDataValidationHelper();
                    CellRangeAddressList addressList = new CellRangeAddressList(1, 100, dropdown.columnIndex, dropdown.columnIndex);
                    if (dropdown.isDateValidation) {
                            // Date Validation
                            String startDateFormula = dropdown.startDate != null ? "DATE(" + dropdown.startDate.replace("-", ",") + ")" : "DATE(2025,1,25)";
                            String endDateFormula = dropdown.endDate != null ? "DATE(" + dropdown.endDate.replace("-", ",") + ")" : "DATE(2025,2,25)";
                            DataValidationConstraint dateConstraint = validationHelper.createDateConstraint(
                                    DataValidationConstraint.OperatorType.BETWEEN,
                                    startDateFormula,
                                    endDateFormula,
                                    "m/d/yy"
                            );
                            DataValidation validation = validationHelper.createValidation(dateConstraint, addressList);
                            validation.setShowErrorBox(true);
                            String errorBoxMessage = (dropdown.startDate == null && dropdown.endDate == null)
                                    ? "Please enter a valid date in m/d/yy format."
                                    : "Please enter a date between " + dropdown.startDate + " and " + dropdown.endDate + " in m/d/yy format.";
                            validation.createErrorBox(INVALID_INPUT, errorBoxMessage);
                            sheet.addValidationData(validation);

                    }  else if (dropdown.isIntegerValidation) {
                        // Integer Validation (Positive or Both)
                        DataValidationConstraint intConstraint;
                        String errorMsg;
                        if (dropdown.onlyPositive) {
                            intConstraint = validationHelper.createIntegerConstraint(
                                    DataValidationConstraint.OperatorType.GREATER_OR_EQUAL, "0", null
                            );
                            errorMsg = "Allows only positive";
                        } else {
                            intConstraint = validationHelper.createIntegerConstraint(
                                    DataValidationConstraint.OperatorType.BETWEEN, "-1000000", "1000000"
                            );
                            errorMsg = "Allows only Integer";
                        }
                        DataValidation intValidation = validationHelper.createValidation(intConstraint, addressList);
                        intValidation.setShowErrorBox(true);
                        intValidation.createErrorBox(INVALID_INPUT, errorMsg);
                        sheet.addValidationData(intValidation);

                    } else {
                        // Regular Dropdown List
                        DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(
                                dropdown.options.toArray(new String[0])
                        );
                        DataValidation validation = validationHelper.createValidation(constraint, addressList);
                        sheet.addValidationData(validation);
                    }
                }
            }
            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }


}
