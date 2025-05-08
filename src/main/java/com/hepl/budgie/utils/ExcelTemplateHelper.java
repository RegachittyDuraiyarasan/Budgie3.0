package com.hepl.budgie.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelTemplateHelper {

	// Constants
	private static final short HEADER_BACKGROUND_COLOR = IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex();
	private static final short BORDER_COLOR = IndexedColors.BLACK.getIndex();
	private static final FillPatternType FILL_PATTERN = FillPatternType.SOLID_FOREGROUND;
	private static final BorderStyle BORDER_STYLE = BorderStyle.THIN;
	private static final int DEFAULT_COLUMN_WIDTH = 20;

	private static final String INVALID_INPUT = "Invalid Input Detected";

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 */
	private ExcelTemplateHelper() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Creates header rows from an enum type where each enum value represents a
	 * header.
	 * 
	 * @param sheet      the sheet where the headers will be created
	 * @param headerEnum the enum class containing header values
	 */
	public static void createHeadersFromEnum(Sheet sheet, Class<? extends Enum<?>> headerEnum) {
		log.info("Creating headers from enum: {}", headerEnum.getSimpleName());
		Enum<?>[] headers = headerEnum.getEnumConstants();
		if (headers == null || headers.length == 0) {
			log.error("No headers found in enum: {}", headerEnum.getSimpleName());
			return;
		}

		CellStyle headerCellStyle = createHeaderCellStyle(sheet.getWorkbook());
		Row headerRow = sheet.createRow(0);
		headerRow.setHeightInPoints(20);

		for (int i = 0; i < headers.length; i++) {
			Enum<?> header = headers[i];
			Cell cell = headerRow.createCell(i);
			String headerValue = getHeaderValue(header);
			log.info("Setting header value: {} for column: {}", headerValue, i);
			cell.setCellValue(headerValue);
			cell.setCellStyle(headerCellStyle);

			int width = getHeaderWidth(header);
			log.info("Setting column width: {} for column: {}", width, i);
			sheet.setColumnWidth(i, width * 256);
		}
	}

	/**
	 * Creates and configures a cell style for header cells in the workbook.
	 * 
	 * @param workbook the workbook where the style will be applied
	 * @return a CellStyle object configured for header cells
	 */
	public static CellStyle createHeaderCellStyle(Workbook workbook) {
		log.info("Creating header cell style...");
		Font headerFont = createHeaderFont(workbook);
		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		headerCellStyle.setFillForegroundColor(HEADER_BACKGROUND_COLOR);
		headerCellStyle.setFillPattern(FILL_PATTERN);
		setBorderStyle(headerCellStyle);
		setAlignment(headerCellStyle);

		return headerCellStyle;
	}

	/**
	 * Creates a header row in the specified sheet with the given headers and column
	 * widths.
	 * 
	 * @param sheet           the sheet where the header row will be created
	 * @param headers         an array of header values to set in the header row
	 * @param columnWidths    an array of column widths (in characters) to set
	 * @param headerCellStyle the cell style to apply to the header cells
	 */
	public static void createHeaderRow(Sheet sheet, String[] headers, int[] columnWidths, CellStyle headerCellStyle) {
		log.info("Creating header row...");
		Row headerRow = sheet.createRow(0);
		headerRow.setHeightInPoints(20);
		for (int i = 0; i < headers.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(headerCellStyle);

			int width = i < columnWidths.length ? columnWidths[i] : DEFAULT_COLUMN_WIDTH;
			sheet.setColumnWidth(i, width * 256);
		}
	}

	/**
	 * Applies the specified cell style to all cells in the given column of the
	 * sheet.
	 * 
	 * @param sheet       the sheet where the cell style will be applied
	 * @param columnIndex the index of the column to which the cell style will be
	 *                    applied
	 * @param cellStyle   the CellStyle to apply to the cells
	 */
	public static void applyColumnDataType(Sheet sheet, int columnIndex, CellStyle cellStyle) {
		log.info("Applying data type to the column of index: {}", columnIndex);
		for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
			Row row = sheet.getRow(rowIndex);
			if (row == null) {
				row = sheet.createRow(rowIndex);
			}
			Cell cell = row.getCell(columnIndex);
			if (cell == null) {
				cell = row.createCell(columnIndex, CellType.NUMERIC);
			}

			cell.setCellStyle(cellStyle);
		}
	}

	/**
	 * Creates a decimal validation for a specified range of columns in the given
	 * sheet.
	 * 
	 * @param sheet       the sheet where the decimal validation will be applied
	 * @param startColumn the index or name of the first column in the range
	 * @param endColumn   the index or name of the last column in the range
	 * @param minValue    the minimum allowable value, set to null for no minimum
	 * @param maxValue    the maximum allowable value, set to null for no maximum
	 */
	public static void createDecimalValidation(Sheet sheet, Object startColumn, Object endColumn, Object minValue,
			Object maxValue) {
		int startColIndex = getColumnIndex(sheet, startColumn);
		int endColIndex = getColumnIndex(sheet, endColumn);

		DataValidationHelper dvHelper = sheet.getDataValidationHelper();

		DataValidationConstraint dvConstraint;
		String errorBoxMessage;

		Double minValueDouble = parseToDouble(minValue);
		Double maxValueDouble = parseToDouble(maxValue);

		if (minValueDouble == null && maxValueDouble == null) {
			// If both are null, we use a custom formula to check if the value is a number
			dvConstraint = dvHelper.createCustomConstraint(
					"AND(ISNUMBER(INDIRECT(ADDRESS(ROW(),COLUMN()))), INDIRECT(ADDRESS(ROW(),COLUMN()))<>\"\")");
			errorBoxMessage = "Please enter a valid number";
		} else {
			String formula1 = minValueDouble == null ? "-9.99E+307" : minValueDouble.toString();
			String formula2 = maxValueDouble == null ? "9.99E+307" : maxValueDouble.toString();
			dvConstraint = dvHelper.createNumericConstraint(DataValidationConstraint.ValidationType.DECIMAL,
					DataValidationConstraint.OperatorType.BETWEEN, formula1, formula2);
			errorBoxMessage = "Please enter a number between " + formula1 + " and " + formula2 + ".";
		}

		CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, startColIndex, endColIndex);
		DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.createErrorBox(INVALID_INPUT, errorBoxMessage);
		sheet.addValidationData(validation);
	}

	/**
	 * Creates a decimal validation for a specified range of columns in the given
	 * sheet.
	 * 
	 * @param sheet       the sheet where the decimal validation will be applied
	 * @param startColumn the index or name of the first column in the range
	 * @param endColumn   the index or name of the last column in the range
	 */
	public static void createDecimalValidation(Sheet sheet, Object startColumn, Object endColumn) {
		createDecimalValidation(sheet, startColumn, endColumn, null, null);
	}

	/**
	 * Creates an integer validation for a specified range of columns in the given
	 * sheet.
	 * 
	 * @param sheet       the sheet where the integer validation will be applied
	 * @param startColumn the index or name of the first column in the range
	 * @param endColumn   the index or name of the last column in the range
	 * @param minValue    the minimum allowable value, set to null for no minimum
	 * @param maxValue    the maximum allowable value, set to null for no maximum
	 */
	public static void createIntegerValidation(Sheet sheet, Object startColumn, Object endColumn, Integer minValue,
			Integer maxValue) {
		int startColIndex = getColumnIndex(sheet, startColumn);
		int endColIndex = getColumnIndex(sheet, endColumn);

		DataValidationHelper dvHelper = sheet.getDataValidationHelper();

		DataValidationConstraint dvConstraint;
		String errorBoxMessage;

		if (minValue == null && maxValue == null) {
			// If both are null, we use a custom formula to check if the value is an integer
			dvConstraint = dvHelper.createCustomConstraint(
					"AND(ISNUMBER(INDIRECT(ADDRESS(ROW(),COLUMN()))), MOD(INDIRECT(ADDRESS(ROW(),COLUMN())),1)=0)");
			errorBoxMessage = "Please enter a valid integer";
		} else {
			String formula1 = minValue == null ? "-2147483648" : minValue.toString(); // Min value for 32-bit integer
			String formula2 = maxValue == null ? "2147483647" : maxValue.toString(); // Max value for 32-bit integer
			dvConstraint = dvHelper.createCustomConstraint("AND(ISNUMBER(INDIRECT(ADDRESS(ROW(),COLUMN()))), "
					+ "MOD(INDIRECT(ADDRESS(ROW(),COLUMN())),1)=0, " + "INDIRECT(ADDRESS(ROW(),COLUMN())) >= "
					+ formula1 + ", " + "INDIRECT(ADDRESS(ROW(),COLUMN())) <= " + formula2 + ")");
			errorBoxMessage = "Please enter an integer between " + formula1 + " and " + formula2 + ".";
		}

		CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, startColIndex, endColIndex);
		DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.createErrorBox(INVALID_INPUT, errorBoxMessage);
		sheet.addValidationData(validation);
	}

	/**
	 * Creates a date validation for a specified range of columns in the given
	 * sheet.
	 * 
	 * @param sheet       the sheet where the date validation will be applied
	 * @param startColumn the index or name of the first column in the range
	 * @param endColumn   the index or name of the last column in the range
	 */
	public static void createIntegerValidation(Sheet sheet, Object startColumn, Object endColumn) {
		createIntegerValidation(sheet, startColumn, endColumn, null, null);
	}

	/**
	 * Creates a date validation for a specified range of columns in the given
	 * sheet.
	 * 
	 * @param sheet          the sheet where the date validation will be applied
	 * @param startColumn    the index or name of the first column in the range
	 * @param endColumn      the index or name of the last column in the range
	 * @param minDateFormula the minimum allowable date formula, set to null for no
	 *                       minimum
	 * @param maxDateFormula the maximum allowable date formula, set to null for no
	 *                       maximum
	 */
	public static void createDateValidation(Sheet sheet, Object startColumn, Object endColumn, String minDateFormula,
			String maxDateFormula) {
		int startColIndex = getColumnIndex(sheet, startColumn);
		int endColIndex = getColumnIndex(sheet, endColumn);

		DataValidationHelper dvHelper = sheet.getDataValidationHelper();

		String minDate = minDateFormula == null ? "DATE(1899, 12, 31)" : minDateFormula;
		String maxDate = maxDateFormula == null ? "DATE(9999, 12, 31)" : maxDateFormula;

		DataValidationConstraint dvConstraint = dvHelper
				.createDateConstraint(DataValidationConstraint.OperatorType.BETWEEN, minDate, maxDate, "m/d/yy");

		CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, startColIndex, endColIndex);
		DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		String errorBoxMessage = (minDateFormula == null && maxDateFormula == null)
				? "Please enter a valid date in m/d/yy format."
				: "Please enter a date between " + minDate + " and " + maxDate + " in m/d/yy format.";
		validation.createErrorBox(INVALID_INPUT, errorBoxMessage);
		sheet.addValidationData(validation);
	}

	/**
	 * Creates a date validation for a specified range of columns in the given
	 * sheet.
	 * 
	 * @param sheet       the sheet where the date validation will be applied
	 * @param startColumn the index or name of the first column in the range
	 * @param endColumn   the index or name of the last column in the range
	 */
	public static void createDateValidation(Sheet sheet, Object startColumn, Object endColumn) {
		createDateValidation(sheet, startColumn, endColumn, null, null);
	}

	/**
	 * Creates a drop-down validation for a specified range of columns in the given
	 * sheet.
	 * 
	 * @param sheet       the sheet where the drop-down validation will be applied
	 * @param startColumn the index or name of the first column in the range
	 * @param endColumn   the index or name of the last column in the range
	 * @param options     an array of options for the drop-down list
	 */
	public static void createDropDownValidation(Sheet sheet, Object startColumn, Object endColumn, String[] options) {
		int startColIndex = getColumnIndex(sheet, startColumn);
		int endColIndex = getColumnIndex(sheet, endColumn);

		DataValidationHelper dvHelper = sheet.getDataValidationHelper();
		DataValidationConstraint dvConstraint = dvHelper.createExplicitListConstraint(options);
		CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, startColIndex, endColIndex);
		DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		validation.createErrorBox("Invalid Selection Detected",
				"Please ensure your selection is from the provided list of options.");
		sheet.addValidationData(validation);
	}

	/**
	 * Creates a drop-down validation for a list of items in a specified column of
	 * the sheet.
	 * 
	 * @param sheet  the sheet where the drop-down validation will be applied
	 * @param items  a list of items to be included in the drop-down
	 * @param mapper a function to map items to their string representation
	 * @param column the name or index of the column to apply the drop-down
	 *               validation
	 */
	public static <T> void createDropDownValidationForList(Sheet sheet, List<T> items, Function<T, String> mapper,
			Object column) {
		if (!items.isEmpty()) {
			String[] itemNames = items.stream().map(mapper).distinct().toArray(String[]::new);
			ExcelTemplateHelper.createDropDownValidation(sheet, column, column, itemNames);
		}
	}

	/**
	 * Creates a dependent drop-down list for a specified range of columns in the
	 * sheet.
	 * 
	 * @param sheet    the sheet where the dependent drop-down will be applied
	 * @param firstCol the index of the first column in the range
	 * @param lastCol  the index of the last column in the range
	 * @param formula  the formula for the dependent drop-down list
	 */
	public static void createDependentDropdown(Sheet sheet, Object startColumn, Object endColumn, String formula) {
		int startColIndex = getColumnIndex(sheet, startColumn);
		int endColIndex = getColumnIndex(sheet, endColumn);

		DataValidationHelper dvHelper = sheet.getDataValidationHelper();
		DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint(formula);
		CellRangeAddressList addressList = new CellRangeAddressList(1, 65535, startColIndex, endColIndex);
		DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		sheet.addValidationData(validation);
	}

	/**
	 * Creates a dependent drop-down list for a specified range of columns in the
	 * sheet.
	 * 
	 * @param sheet   the sheet where the dependent drop-down will be applied
	 * @param column  the index or name of the column where the dependent drop-down
	 *                will be applied
	 * @param formula the formula for the dependent drop-down list
	 */
	public static void createDependentDropdown(Sheet sheet, Object column, String formula) {
		createDependentDropdown(sheet, column, column, formula);
	}

	/**
	 * Writes the given workbook to a byte array.
	 * 
	 * @param workbook the workbook to write
	 * @return a byte array representation of the workbook
	 * @throws IOException if an I/O error occurs during writing
	 */
	public static byte[] writeWorkbookToByteArray(Workbook workbook) throws IOException {
		log.info("Writing workbook to byte array...");
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
			workbook.write(byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		}
	}

	// Helper Methods
	private static Font createHeaderFont(Workbook workbook) {
		log.info("Creating header font...");
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);

		return headerFont;
	}

	private static void setBorderStyle(CellStyle cellStyle) {
		log.info("Setting border style...");
		cellStyle.setBorderBottom(BORDER_STYLE);
		cellStyle.setBottomBorderColor(BORDER_COLOR);
		cellStyle.setBorderLeft(BORDER_STYLE);
		cellStyle.setLeftBorderColor(BORDER_COLOR);
		cellStyle.setBorderTop(BORDER_STYLE);
		cellStyle.setTopBorderColor(BORDER_COLOR);
		cellStyle.setBorderRight(BORDER_STYLE);
		cellStyle.setRightBorderColor(BORDER_COLOR);
	}

	private static void setAlignment(CellStyle cellStyle) {
		log.info("Setting alignment...");
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setLocked(true);
	}

	private static int getColumnIndex(Sheet sheet, Object column) {
		if (column instanceof Integer) {
			return (int) column;
		} else if (column instanceof String) {
			String lowerColumn = ((String) column).toLowerCase();
			Row headerRow = sheet.getRow(0);
			for (Cell cell : headerRow) {
				if (cell.getStringCellValue().toLowerCase().equals(lowerColumn)) {
					return cell.getColumnIndex();
				}
			}
		}
		throw new IllegalArgumentException("Column not found: " + column);
	}

	private static String getHeaderValue(Enum<?> header) {
		try {
			Method getValue = header.getClass().getMethod("getValue");
			String value = (String) getValue.invoke(header);
			log.info("Got header value: {} for header: {}", value, header.name());
			return value;
		} catch (Exception e) {
			log.error("Error getting header value for {}: {}", header.name(), e.getMessage());
			return header.name();
		}
	}

	private static int getHeaderWidth(Enum<?> header) {
		try {
			Method getWidth = header.getClass().getMethod("getWidth");
			int width = (int) getWidth.invoke(header);
			log.info("Got header width: {} for header: {}", width, header.name());
			return width;
		} catch (Exception e) {
			log.error("Error getting header width for {}: {}", header.name(), e.getMessage());
			return DEFAULT_COLUMN_WIDTH;
		}
	}

	private static Double parseToDouble(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		}
		if (value instanceof String) {
			try {
				return Double.parseDouble((String) value);
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}
}
