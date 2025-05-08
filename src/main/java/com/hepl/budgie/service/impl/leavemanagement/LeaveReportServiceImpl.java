package com.hepl.budgie.service.impl.leavemanagement;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.leavemanagement.LeaveReportDTO;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.leavemanagement.LeaveReportRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.leavemanagement.LeaveReportService;
import com.hepl.budgie.utils.PayrollMonthProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class LeaveReportServiceImpl implements LeaveReportService {
	private final LeaveReportRepository leaveReportRepository;
	private final UserInfoRepository userInfoRepository;
	private final MongoTemplate mongoTemplate;
	private final LeaveApplyRepo leaveApplyRepo;
	private final PayrollLockMonthRepository payrollLockMonthRepository;
	private final PayrollMonthProvider payrollMonthProvider;
	private final JWTHelper jwtHelper;

	@Override
	public List<LeaveReportDTO> getLeaveReportList(String yearMonth) {
		String[] parts = yearMonth.split("-");
		int month = Integer.parseInt(parts[0]);
		int year = Integer.parseInt(parts[1]);

		PayrollMonth payRollMonth = payrollLockMonthRepository.findPayrollMonthByMonthYear(yearMonth,
				jwtHelper.getOrganizationCode(), mongoTemplate, "IN");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String fromDate = payRollMonth.getStartDate().format(formatter);
		String toDate = payRollMonth.getEndDate().format(formatter);

		List<LeaveApply> leaveApplies = leaveApplyRepo.findLeavesWithDatesInRange(fromDate, toDate,
				jwtHelper.getOrganizationCode(), mongoTemplate);

		List<LeaveReportDTO> leaveReportDtos = new ArrayList<>();
		for (LeaveApply leaveApply : leaveApplies) {
			UserInfo userInfo = userInfoRepository.findByEmpId(leaveApply.getEmpId()).orElse(null);
			UserInfo reportingManager = userInfoRepository.findByEmpId(leaveApply.getAppliedTo()).orElse(null);

			// if (getEmpName.isPresent() && getReportingManagerName.isPresent()) {
			// UserInfo user = getEmpName.get();
			// UserInfo reportingName = getReportingManagerName.get();

			String empName = userInfo.getSections().getBasicDetails().getFirstName() + " "
					+ userInfo.getSections().getBasicDetails().getLastName();
			String reportingTo = reportingManager.getSections().getBasicDetails().getFirstName() + " "
					+ reportingManager.getSections().getBasicDetails().getLastName();
			// LocalDate postedAtDate = leaveApply.getCreatedAt().toLocalDate();
			// LocalDate parsedFromDate = LocalDate.parse(leaveApply.getFromDate());
			// LocalDate parsedToDate = LocalDate.parse(leaveApply.getToDate());

			LocalDate postedAtDate = LocalDate.now();
			LocalDate parsedFromDate = LocalDate.parse(leaveApply.getFromDate());
			LocalDate parsedToDate = LocalDate.parse(leaveApply.getToDate());

			LeaveReportDTO leaveReportDto = new LeaveReportDTO();
			leaveReportDto.setId(leaveApply.getId());
			leaveReportDto.setEmpId(leaveApply.getEmpId());
			leaveReportDto.setEmpName(empName);
			leaveReportDto.setAppliedTo(leaveApply.getAppliedTo());
			leaveReportDto.setReportingToName(reportingTo);
			leaveReportDto.setLeaveType(leaveApply.getLeaveType());
			leaveReportDto.setLeaveCategory(leaveApply.getLeaveCategory());
			leaveReportDto.setNumOfDays(leaveApply.getNumOfDays());
			leaveReportDto.setFromDate(parsedFromDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
			leaveReportDto.setToDate(parsedToDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
			leaveReportDto.setStatus(leaveApply.getStatus());
			leaveReportDto.setPostedAt(postedAtDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
			leaveReportDto.setReason(leaveApply.getEmpReason());
			leaveReportDto.setRemarks(leaveApply.getApproverReason());
			leaveReportDtos.add(leaveReportDto);
			// }
		}

		return leaveReportDtos;
	}

}
