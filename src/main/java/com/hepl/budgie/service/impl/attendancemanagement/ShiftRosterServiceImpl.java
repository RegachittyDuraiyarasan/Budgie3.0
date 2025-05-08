package com.hepl.budgie.service.impl.attendancemanagement;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.IOException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.attendancemanagement.DayType;
import com.hepl.budgie.entity.attendancemanagement.RosterDetails;
import com.hepl.budgie.entity.attendancemanagement.ShiftMaster;
import com.hepl.budgie.entity.attendancemanagement.ShiftRoster;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.enums.DayTypeHeader;
import com.hepl.budgie.repository.attendancemanagement.DayTypeRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftMasterRepository;
import com.hepl.budgie.repository.attendancemanagement.ShiftRosterRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.attendancemanagement.ShiftRosterService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.ExcelTemplateHelper;
import com.mongodb.bulk.BulkWriteResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ShiftRosterServiceImpl implements ShiftRosterService {

	private final UserInfoRepository userInfoRepository;
	private final ShiftRosterRepository shiftRosterRepository;
	private final ShiftMasterRepository shiftMasterRepository;
	private final DayTypeRepository dayTypeRepository;
	private final JWTHelper jwtHelper;
	private final MongoTemplate mongoTemplate;

	@Override
	public List<ShiftRoster> fetch(String monthYear, String empId) {
		log.info("Fetching Shift Roster");
		// List<ShiftRoster> shiftRoster = shiftRosterRepository.findAll();
		String orgId = jwtHelper.getOrganizationCode();
		List<UserInfo> users = userInfoRepository.findByStatus(Status.ACTIVE.label);

		return shiftRosterRepository.finfByEmpIdAndMonthYear(orgId, monthYear, empId, users, mongoTemplate);
	}

	@Override
	public byte[] shiftRosterTemplate(String monthYear) {

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Shift Roster");

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
			YearMonth yearMonth = YearMonth.parse(monthYear, formatter);

			int daysInMonth = yearMonth.lengthOfMonth();

			String[] headers = new String[daysInMonth + 3];
			headers[0] = "Employee Id";
			headers[1] = "Month";
			headers[2] = "Year";

			for (int i = 1; i <= daysInMonth; i++) {
				headers[i + 2] = String.valueOf(i);
			}

			CellStyle headerCellStyle = ExcelTemplateHelper.createHeaderCellStyle(workbook);
			Row headerRow = sheet.createRow(0);

			for (int i = 0; i < headers.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(headers[i]);
				cell.setCellStyle(headerCellStyle);
			}

			List<ShiftMaster> shiftCode = shiftMasterRepository.findByStatusTrue();

			for (int i = 3; i < headers.length; i++) {
				ExcelTemplateHelper.createDropDownValidationForList(sheet, shiftCode, ShiftMaster::getShiftCode, i);
			}

			log.info("Excel Template Created Sucessfully");

			return ExcelTemplateHelper.writeWorkbookToByteArray(workbook);

		} catch (Exception e) {
			log.error("Error in createExcelTemplate: ", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					AppMessages.EXCEL_TEMPLATE_CREATION_FAILED);
		}
	}

	@Override
	public byte[] shiftRosterDayType() {
		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Day Type");
			ExcelTemplateHelper.createHeadersFromEnum(sheet, DayTypeHeader.class);

			List<DayType> dayTypes = dayTypeRepository.findByStatusTrue();
			List<ShiftMaster> master = shiftMasterRepository.findByStatusTrue();

			ExcelTemplateHelper.createDropDownValidationForList(sheet, dayTypes, DayType::getDayType, "Day Type");
			ExcelTemplateHelper.createDropDownValidationForList(sheet, master, ShiftMaster::getShiftCode, "Shift Code");
			log.info("Excel Template Created Sucessfully");

			return ExcelTemplateHelper.writeWorkbookToByteArray(workbook);

		} catch (Exception e) {
			log.error("Error in createExcelTemplate: ", e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					AppMessages.EXCEL_TEMPLATE_CREATION_FAILED);
		}
	}

	@Override
	public Map<String, List<String>> importShiftRoaster(MultipartFile file) throws IOException {
		String orgId = jwtHelper.getOrganizationCode();
		Map<String, List<String>> errors = new HashMap<>();
		List<ShiftRoster> recordsToUpdate = new ArrayList<>();

		try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) { 
			Sheet sheet = workbook.getSheetAt(0);

			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				Row row = sheet.getRow(i);
				if (row == null)
					continue;

				String empId = getCellValue(row.getCell(0));
				String month = getCellValue(row.getCell(1));
				String year = getCellValue(row.getCell(2));

				if (empId == null || month == null || year == null) {
					errors.computeIfAbsent("Row " + (i + 1), k -> new ArrayList<>()).add("Missing mandatory fields");
					continue;
				}

				// if (!userInfoRepository.existsByEmpId(empId)) {
				// 	errors.computeIfAbsent(empId, k -> new ArrayList<>()).add("Employee ID not found");
				// 	continue;
				// }

				String monthYear = formatMonthYear(month, year);
				ShiftRoster shiftRoster = shiftRosterRepository.findByMonthYearAndEmpId(monthYear, empId, orgId,
						mongoTemplate);
				if (shiftRoster == null) {
					shiftRoster = new ShiftRoster(null, empId, monthYear, new ArrayList<>(), null, null);
				}

				for (int colIndex = 3; colIndex < row.getPhysicalNumberOfCells(); colIndex++) {
					String shift = getCellValue(row.getCell(colIndex));
					if (shift != null && !shift.isBlank()) {
						int date = colIndex - 2;
						updateRosterDetails(shiftRoster, date, shift, month, year);
					}
				}
				recordsToUpdate.add(shiftRoster);
			}

			if (!recordsToUpdate.isEmpty()) {
				shiftRosterRepository.saveShiftRosterBulk(recordsToUpdate, orgId, mongoTemplate);
			}
		}
		return errors;
	}

	private void updateRosterDetails(ShiftRoster shiftRoster, int date, String shift, String month, String year) {

		LocalDate formattedDate = LocalDate.of(Integer.parseInt(year), getMonthNumber(month), date);
		Optional<RosterDetails> existingEntry = shiftRoster.getRosterDetails().stream()
				.filter(d -> d.getDate().equals(formattedDate))
				.findFirst();

		if (existingEntry.isPresent()) {
			RosterDetails rosterDetail = existingEntry.get();
			rosterDetail.setShift(shift);
		} else {
			shiftRoster.getRosterDetails().add(new RosterDetails(formattedDate, shift));
		}
	}

	private int getMonthNumber(String month) {
		List<String> months = List.of("January", "February", "March", "April", "May", "June",
				"July", "August", "September", "October", "November", "December");
		return months.indexOf(month) + 1; 
	}

	private String formatMonthYear(String month, String year) {
		List<String> months = List.of("January", "February", "March", "April", "May", "June",
				"July", "August", "September", "October", "November", "December");
		int monthIndex = months.indexOf(month) + 1;
		return String.format("%02d-%s", monthIndex, year);
	}

	private String getCellValue(Cell cell) {
		if (cell == null)
			return null;
		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue().trim();
			case NUMERIC -> String.valueOf((int) cell.getNumericCellValue());
			default -> null;
		};
	}

	@Override
	public BulkWriteResult excelBulkImport(List<Map<String, Object>> validRows) {
		
		if (validRows.isEmpty()) {
            return BulkWriteResult.unacknowledged();
        }
        String orgId = jwtHelper.getOrganizationCode();
        return shiftRosterRepository.shiftRosterBulkUpsert(mongoTemplate,
                orgId,
                validRows);
	}

}
