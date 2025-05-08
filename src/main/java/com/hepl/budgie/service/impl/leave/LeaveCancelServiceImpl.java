package com.hepl.budgie.service.impl.leave;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.entity.LeaveCategory;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.YesOrNoEnum;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leave.LeaveApplyDates;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.service.leave.LeaveCancelService;
import com.hepl.budgie.utils.IdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveCancelServiceImpl implements LeaveCancelService {

	private final JWTHelper jwtHelper;
	private final Translator translator;
	private final IdGenerator idGenerator;
	private final MongoTemplate mongoTemplate;
	private final LeaveApplyRepo leaveApplyRepo;
	private final PayrollLockMonthRepository payrollLockMonthRepository;

	@Override
	public List<LeaveApply> getLeaveApproved() {
		log.info("Fetching Leave Approved Data for current month");
		String empId = jwtHelper.getUserRefDetail().getEmpId();
		PayrollMonth month = payrollLockMonthRepository.getLockedPayrollMonth(mongoTemplate,
				jwtHelper.getOrganizationCode(), "IN");

		ZonedDateTime startDate = month.getStartDate();
		ZonedDateTime endDate = month.getEndDate();

		List<LeaveApply> leaveList = leaveApplyRepo.findByEmpIdAndStatusAndLeaveCategoryAndLeaveCancelWithDateRange(
				empId, "Approved", "Leave Apply", "No", startDate, endDate, jwtHelper.getOrganizationCode(),
				mongoTemplate);

		return leaveList;
	}

	@Override
	public void leaveCancel(String leaveCode, List<String> appliedCC, String reason) {
		log.info("Leave Cancel for the code : {}", leaveCode);
		UserRef empData = jwtHelper.getUserRefDetail();
		String org = jwtHelper.getOrganizationCode();

		LeaveApply originalLeave = leaveApplyRepo.findByLeaveCodeAndEmpIdAndStatusAndLeaveCategoryAndLeaveCancel(
				leaveCode, empData.getEmpId(), Status.APPROVED.label, LeaveCategory.LEAVE_APPLY.label,
				YesOrNoEnum.NO.label, empData.getOrganizationCode(), mongoTemplate);

		if (originalLeave == null) {
			String errorMessage = "No approved leave found with code: " + leaveCode + " for employee: "
					+ empData.getEmpId();
			log.warn(errorMessage);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage);
		}

		LeaveApply cancellationRequest = createCancellationRequest(originalLeave, empData, appliedCC, reason);
		leaveApplyRepo.saveLeaveApply(cancellationRequest, org, mongoTemplate);

		originalLeave.setLeaveCancel(YesOrNoEnum.YES.label);
		leaveApplyRepo.saveLeaveApply(originalLeave, org, mongoTemplate);
	}

	private LeaveApply createCancellationRequest(LeaveApply original, UserRef empData, List<String> appliedCC,
			String reason) {
		String code = idGenerator.generateLeaveCode(empData.getOrganizationCode());
		return LeaveApply.builder().leaveCode(code).empId(empData.getEmpId()).appliedTo(original.getAppliedTo())
				.appliedCC(appliedCC).leaveType(original.getLeaveType()).leaveCategory(LeaveCategory.LEAVE_CANCEL.label)
				.empReason(reason).numOfDays(original.getNumOfDays()).balance(original.getBalance())
				.leaveCancel(YesOrNoEnum.NO.label).leaveApply(updateDateDetailsToPending(original.getLeaveApply()))
				.dateList(original.getDateList()).fromDate(original.getFromDate()).toDate(original.getToDate())
				.fromSession(original.getFromSession()).toSession(original.getToSession()).status(Status.PENDING.label)
				.build();
	}

	private List<LeaveApplyDates> updateDateDetailsToPending(List<LeaveApplyDates> originalDates) {
		return originalDates.stream().map(date -> {
			date.setStatus(Status.PENDING.label);
			date.setApproverRemarks(null);
			return date;
		}).toList();
	}
}
