package com.hepl.budgie.utils;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

public class PdfGenerator {

	private static final DeviceRgb BORDER_COLOR = new DeviceRgb(0, 0, 0);

	public static byte[] generatePdf(List<String> headers, List<List<Object>> data, Map<String, String> metadata) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
			Document document = new Document(pdfDocument);
			PdfFont timesNewRoman = PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
			PdfFont timesNewRomanBold = PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);

			System.out.println(metadata);
			document.setFont(timesNewRoman);
			document.setFontSize(10);

			Paragraph title = new Paragraph(metadata.get("Company Name")).setFont(timesNewRomanBold).setFontSize(16)
					.setTextAlignment(TextAlignment.CENTER).setFontColor(new DeviceRgb(54, 57, 149));
			document.add(title);

			// Address Section
			Paragraph address = new Paragraph(metadata.get("Company Address")).setFontSize(8)
					.setTextAlignment(TextAlignment.CENTER);
			document.add(address);

			// Report Period Section
			Paragraph reportTitle = new Paragraph(metadata.get("Report Period")).setFontSize(8)
					.setTextAlignment(TextAlignment.CENTER).setMarginBottom(10);
			document.add(reportTitle);

			// Employee Info Table
			Table empInfoTable = createEmployeeInfoTable(metadata, timesNewRomanBold);
			document.add(empInfoTable.setMarginBottom(10));

			// Data Table Header
			Table dataTable = createDataTable(headers, data, timesNewRomanBold);
			document.add(dataTable);

			// Close the document and return the byte array
			document.close();
			return outputStream.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Error while generating PDF report", e);
		}
	}

	// Helper method to create the Employee Info Table
	private static Table createEmployeeInfoTable(Map<String, String> metadata, PdfFont boldFont) {
		float[] empInfoColumnWidths = { 3, 4, 3, 4 };
		Table empInfoTable = new Table(UnitValue.createPercentArray(empInfoColumnWidths)).useAllAvailableWidth()
				.setBorder(new SolidBorder(BORDER_COLOR, 1));

		empInfoTable.addCell(createSimpleHeaderCell("Name :", boldFont));
		empInfoTable.addCell(createSimpleCell(metadata.get("Employee Name")));
		empInfoTable.addCell(createSimpleHeaderCell("Employee No:", boldFont));
		empInfoTable.addCell(createSimpleCell(metadata.get("Employee ID")));

		empInfoTable.addCell(createSimpleHeaderCell("Department :", boldFont));
		empInfoTable.addCell(createSimpleCell(metadata.get("Department")));
		empInfoTable.addCell(createSimpleHeaderCell("DOJ :", boldFont));
		empInfoTable.addCell(createSimpleCell(metadata.get("Date of Joining")));

		empInfoTable.addCell(createSimpleHeaderCell("Rep. Manager :", boldFont));
		empInfoTable.addCell(createSimpleCell(metadata.get("Reporting Manager")));
		empInfoTable.addCell(createSimpleHeaderCell("Rep. Manager Dept :", boldFont));
		empInfoTable.addCell(createSimpleCell(metadata.get("Manager Department")));

		return empInfoTable;
	}

	// Helper method to create the Data Table with headers and rows
	private static Table createDataTable(List<String> headers, List<List<Object>> data, PdfFont boldFont) {
		float[] columnWidths = new float[headers.size()];
		for (int i = 0; i < columnWidths.length; i++) {
			columnWidths[i] = 1;
		}

		Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
		for (String header : headers) {
			table.addCell(createHeaderCell(header, boldFont));
		}

		// Add data rows
		for (List<Object> row : data) {
			for (Object cellValue : row) {
				table.addCell(createValueCell(cellValue.toString()));
			}
		}

		return table;
	}

	// Header Cell Style with Border Color
	private static Cell createHeaderCell(String text, PdfFont boldFont) {
		return new Cell().add(new Paragraph(text).setFont(boldFont))
				.setBackgroundColor(new DeviceRgb(0.75f, 0.75f, 0.75f)).setTextAlignment(TextAlignment.CENTER)
				.setPadding(5);
	}

	// Value Cell Style with Border Color
	private static Cell createValueCell(String text) {
		return new Cell().add(new Paragraph(text)).setTextAlignment(TextAlignment.LEFT).setPadding(5);
	}

	// Simple Cell (for employee info without borders)
	private static Cell createSimpleCell(String text) {
		return new Cell().add(new Paragraph(text)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT)
				.setPadding(5);
	}

	// Simple Header Cell (for employee info without borders)
	private static Cell createSimpleHeaderCell(String text, PdfFont boldFont) {
		return new Cell().add(new Paragraph(text).setFont(boldFont)).setBorder(Border.NO_BORDER)
				.setTextAlignment(TextAlignment.LEFT).setPadding(5);
	}
}