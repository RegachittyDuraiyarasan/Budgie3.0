package com.hepl.budgie.service.impl.leavemanagement;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LockAttendanceDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.repository.leavemanagement.LockAttendanceRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.service.leavemanagement.LockAttendanceService;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LockAttendanceServiceImpl implements LockAttendanceService {
	private final LockAttendanceRepository lockAttendanceRepository;
	private final PayrollLockMonthRepository payrollLockMonthRepository;
	private final MongoTemplate mongoTemplate;
	private final JWTHelper jwtHelper;

	public LockAttendanceServiceImpl(LockAttendanceRepository lockAttendanceRepository,
			PayrollLockMonthRepository payrollLockMonthRepository, MongoTemplate mongoTemplate, JWTHelper jwtHelper) {
		this.lockAttendanceRepository = lockAttendanceRepository;
		this.payrollLockMonthRepository = payrollLockMonthRepository;
		this.mongoTemplate = mongoTemplate;
		this.jwtHelper = jwtHelper;
	}

	@Override
	public List<LockAttendanceDTO> getAttendanceDateList() {
		String org = jwtHelper.getOrganizationCode();
		List<PayrollLockMonth> activePayrollLockMonth = payrollLockMonthRepository
				.getPayrollByOrgAndStatus(mongoTemplate, org, "IN", Status.ACTIVE.label);
		List<LockAttendanceDTO> lockAttendanceDTOs = activePayrollLockMonth.stream().map(entity -> {
			LockAttendanceDTO dto = new LockAttendanceDTO();
			dto.setId(entity.getId());
			dto.setStandardStartDate(entity.getStandardStartDate());
			dto.setStandardEndDate(entity.getStandardEndDate());
			dto.setAttendanceLockDate(entity.getAttendanceLockDate());
			dto.setAttendanceEmpLockDate(entity.getAttendanceEmpLockDate());
			dto.setAttendanceRepoLockDate(entity.getAttendanceRepoLockDate());
			dto.setOrgId(entity.getOrgId());
			return dto;
		}).collect(Collectors.toList());

		return lockAttendanceDTOs;
	}

	@Override
	public void updateLockAttendanceDate(String id, FormRequest formRequest) {
		PayrollLockMonth existingRecord = payrollLockMonthRepository
				.findById(id, jwtHelper.getOrganizationCode(), mongoTemplate, "IN")
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
		System.out.println("formrequest:" + formRequest.getLockAttendanceDate());
		existingRecord.setAttendanceLockDate((String) formRequest.getLockAttendanceDate());
		String col = payrollLockMonthRepository.getCollectionName("IN");
		mongoTemplate.save(existingRecord, col);
	}

	@Override
	public String lockAttendance(String attendanceEmpLockDate, String attendanceRepoLockDate, String org) {

		PayrollLockMonth payrollLock = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate,
				jwtHelper.getOrganizationCode(), "IN");
		payrollLock.setAttendanceEmpLockDate(attendanceEmpLockDate);
		payrollLock.setAttendanceRepoLockDate(attendanceRepoLockDate);
		payrollLock.setOrgId(org);
		String col = payrollLockMonthRepository.getCollectionName("IN");
		mongoTemplate.save(payrollLock, col);
		return "success";
	}

	@Override
	public String lockDateUpdate(String id, String attendanceEmpLockDate, String attendanceRepoLockDate, String org1) {

		String org = jwtHelper.getOrganizationCode();
		String col = payrollLockMonthRepository.getCollectionName("IN");
		Criteria criteria = new Criteria().andOperator(Criteria.where("_id").is(id), Criteria.where("orgId").is(org));

		Query query = new Query(criteria);
		PayrollLockMonth existingRecord = mongoTemplate.findOne(query, PayrollLockMonth.class, col);
		existingRecord.setAttendanceEmpLockDate(attendanceEmpLockDate);
		existingRecord.setAttendanceRepoLockDate(attendanceRepoLockDate);
		existingRecord.setOrgId(org1);
		mongoTemplate.save(existingRecord, col);
		return "success";
	}

}
