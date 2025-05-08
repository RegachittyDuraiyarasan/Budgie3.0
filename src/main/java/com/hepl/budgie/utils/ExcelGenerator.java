package com.hepl.budgie.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class ExcelGenerator {

	public static byte[] generateExcel(List<String> headers, List<List<Object>> data, Map<String, String> metadata) {
		try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			Sheet sheet = workbook.createSheet("Leave Transactions");

			int rowIndex = 0;

			Row titleRow = sheet.createRow(rowIndex++);
			createCell(titleRow, 0, metadata.get("Company Name"), workbook, true, 18,
					HorizontalAlignment.CENTER, IndexedColors.LIGHT_CORNFLOWER_BLUE);
			sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, headers.size() - 1));

			Row addressRow = sheet.createRow(rowIndex++);
			createCell(addressRow, 0, metadata.get("Company Address"), workbook, false, 12,
					HorizontalAlignment.CENTER, null);
			sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, headers.size() - 1));

			Row dateRangeRow = sheet.createRow(rowIndex++);
			createCell(dateRangeRow, 0, metadata.get("Report Period"), workbook, false, 12,
					HorizontalAlignment.CENTER, null);
			sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, headers.size() - 1));

			Row empInfoRow1 = sheet.createRow(rowIndex++);
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow1.getRowNum(), empInfoRow1.getRowNum(), 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow1.getRowNum(), empInfoRow1.getRowNum(), 2, 3));
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow1.getRowNum(), empInfoRow1.getRowNum(), 4, 5));
			createCell(empInfoRow1, 0, "Name :", workbook, true, 12, HorizontalAlignment.LEFT, null);
			createCell(empInfoRow1, 2, metadata.get("Employee Name"), workbook, false, 12, HorizontalAlignment.LEFT,
					null);
			createCell(empInfoRow1, 4, "Employee No :", workbook, true, 12, HorizontalAlignment.LEFT, null);
			createCell(empInfoRow1, 6, metadata.get("Employee ID"), workbook, false, 12, HorizontalAlignment.LEFT,
					null);

			Row empInfoRow2 = sheet.createRow(rowIndex++);
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow2.getRowNum(), empInfoRow2.getRowNum(), 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow2.getRowNum(), empInfoRow2.getRowNum(), 2, 3));
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow2.getRowNum(), empInfoRow2.getRowNum(), 4, 5));
			createCell(empInfoRow2, 0, "Department :", workbook, true, 12, HorizontalAlignment.LEFT, null);
			createCell(empInfoRow2, 2, metadata.get("Department"), workbook, false, 12, HorizontalAlignment.LEFT, null);
			createCell(empInfoRow2, 4, "DOJ :", workbook, true, 12, HorizontalAlignment.LEFT, null);
			createCell(empInfoRow2, 6, metadata.get("Date of Joining"), workbook, false, 12, HorizontalAlignment.LEFT,
					null);

			Row empInfoRow3 = sheet.createRow(rowIndex++);
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow3.getRowNum(), empInfoRow3.getRowNum(), 0, 1));
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow3.getRowNum(), empInfoRow3.getRowNum(), 2, 3));
			sheet.addMergedRegion(new CellRangeAddress(empInfoRow3.getRowNum(), empInfoRow3.getRowNum(), 4, 5));
			createCell(empInfoRow3, 0, "Rep.Manager :", workbook, true, 12, HorizontalAlignment.LEFT, null);
			createCell(empInfoRow3, 2, metadata.get("Reporting Manager"), workbook, false, 12, HorizontalAlignment.LEFT,
					null);
			createCell(empInfoRow3, 4, "Rep.Manager Dept :", workbook, true, 12, HorizontalAlignment.LEFT, null);
			createCell(empInfoRow3, 6, metadata.get("Manager Department"), workbook, false, 12,
					HorizontalAlignment.LEFT, null);

			Row headerRow = sheet.createRow(rowIndex++);
			for (int i = 0; i < headers.size(); i++) {
				createCell(headerRow, i, headers.get(i), workbook, true, 12, HorizontalAlignment.CENTER,
						IndexedColors.GREY_25_PERCENT);
			}

			for (List<Object> rowData : data) {
				Row dataRow = sheet.createRow(rowIndex++);
				for (int i = 0; i < rowData.size(); i++) {
					createCell(dataRow, i, rowData.get(i).toString(), workbook, false, 10, HorizontalAlignment.LEFT,
							null);
				}
			}

			for (int i = 0; i < headers.size(); i++) {
				sheet.autoSizeColumn(i);
			}

			workbook.write(outputStream);
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Error while generating Excel report", e);
		}
	}

	private static void createCell(Row row, int column, String value, Workbook workbook, boolean isHeader, int fontSize,
			HorizontalAlignment alignment, IndexedColors bgColor) {
		Cell cell = row.createCell(column);
		cell.setCellValue(value);

		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(isHeader);
		font.setFontHeightInPoints((short) fontSize);
		style.setFont(font);
		style.setAlignment(alignment);
		if (bgColor != null) {
			style.setFillForegroundColor(bgColor.getIndex());
			style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		}
		cell.setCellStyle(style);
	}
}
