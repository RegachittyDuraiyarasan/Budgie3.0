package com.hepl.budgie.service.impl.leavemanagement;

import java.util.List;
import java.util.Optional;

import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.entity.leavemanagement.LeaveBalanceSummary;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.repository.leavemanagement.LeaveMasterRepository;
import com.hepl.budgie.service.leavemanagement.LeaveMasterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaveMasterServiceImpl implements LeaveMasterService {

	private final LeaveMasterRepository leaveMasterRepository;
	private final MongoTemplate mongoTemplate;
	private final JWTHelper jwtHelper;

	@Override
	public double employeeLeaveBalance(String empId, String year, String leaveType) {
		log.info("Fetching Leave balance for employee");
		try {
			double balance = 0;
			LeaveMaster leaveMaster = leaveMasterRepository
					.findByEmpIdAndYear(empId, year, jwtHelper.getOrganizationCode(), mongoTemplate)
					.orElseThrow(() -> new ResourceNotFoundException(
							String.format("LeaveMaster not found for employee ID: %s and year: %s", empId, year)));

			List<LeaveTransactions> leaveTransactionList = leaveMaster.getLeaveTransactions();
			Optional<LeaveTransactions> foundTransactionList = leaveTransactionList.stream()
					.filter(lvTransList -> lvTransList.getLeaveTypeName().equalsIgnoreCase(leaveType)).findFirst();
			balance = foundTransactionList.get().getNoOfDays();

			return balance;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public List<LeaveTransactions> fetchLeaveTransactionByYearAndType(String empId, String year, String leaveType) {
		log.info("Fetching Leave Transactions ");
		LeaveMaster leave = leaveMasterRepository
				.findByEmpIdAndYearAndLeaveType(empId, year, leaveType, jwtHelper.getOrganizationCode(), mongoTemplate)
				.orElseThrow(() -> new ResourceNotFoundException(
						String.format("LeaveMaster not found for employee ID: %s , year: %s and leaveType : %s", empId,
								year, leaveType)));

		return leave.getLeaveTransactions();
	}

	@Override
	public Optional<LeaveBalanceSummary> getLeaveBalanceSummary(String empId, String year, String leaveType) {
		Optional<LeaveMaster> leaveMaster = leaveMasterRepository.findLeaveBalanceSummaryByEmpIdAndYearAndLeaveType(
				empId, year, leaveType, jwtHelper.getOrganizationCode(), mongoTemplate);
		return leaveMaster.map(master -> master.getLeaveBalanceSummary().get(0));
	}

}
