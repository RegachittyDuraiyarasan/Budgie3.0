package com.hepl.budgie.utils;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leavemanagement.LeaveMaster;
import com.hepl.budgie.entity.leavemanagement.LeaveTransactions;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.hepl.budgie.repository.leave.LeaveApplyRepo;
import com.hepl.budgie.repository.leavemanagement.LeaveTypeCategoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdGenerator {

	private final LeaveTypeCategoryRepository leaveTypeCategoryRepository;
	private final LeaveApplyRepo leaveApplyRepository;
	private final MongoTemplate mongoTemplate;
	private final ReentrantLock lock = new ReentrantLock();

	private static final String LEAVE_APPLY_CODE_PREFIX = "LA";
	private static final int LEAVE_APPLY_CODE_LENGTH = 7;
	private static final String LEAVE_APPLY_INITIAL_CODE = LEAVE_APPLY_CODE_PREFIX + "00001";

	private static final String LEAVE_TYPE_CATEGORY_PREFIX = "LTC";
	private static final int LEAVE_TYPE_CATEGORY_LENGTH = 6;
	private static final String LEAVE_TYPE_CATEGORY_INITIAL_CODE = LEAVE_TYPE_CATEGORY_PREFIX + "001";

	private static final String LEAVE_TRANSACTION_PREFIX = "T";
	private static final String LEAVE_TRANSACTION_INITIAL_CODE = "T001";

	public static String generateFamilyId(int count) {
		return String.format("FM%04d", count);
	}

	/**
	 * Generates sequential leave application code (LAXXXXX) Thread-safe with
	 * database fallback
	 * 
	 * @param org           Organization ID
	 * @param mongoTemplate MongoDB template
	 * @return Generated leave application code
	 */
	public String generateLeaveCode(String org) {
		lock.lock();
		try {
			return leaveApplyRepository.findTopByOrderByLeaveCodeDesc(org, mongoTemplate).map(this::incrementLeaveCode)
					.orElse(LEAVE_APPLY_INITIAL_CODE);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Generates unique leave type category code (LTCXXX) Thread-safe with
	 * organization isolation
	 * 
	 * @param orgId Organization ID
	 * @return Generated leave type code
	 */
	public String generateLeaveTypeCategoryCode(String orgId) {
		lock.lock();
		try {
			return leaveTypeCategoryRepository.findLatestLeaveType(orgId, mongoTemplate)
					.map(LeaveTypeCategory::getLeaveUniqueCode).map(this::incrementLeaveTypeCode)
					.orElse(LEAVE_TYPE_CATEGORY_INITIAL_CODE);
		} finally {
			lock.unlock();
		}
	}

	private String incrementLeaveCode(LeaveApply lastLeave) {
		try {
			String lastCode = lastLeave.getLeaveCode();
			if (isValidCode(lastCode, LEAVE_APPLY_CODE_PREFIX, LEAVE_APPLY_CODE_LENGTH)) {
				int lastNumber = Integer.parseInt(lastCode.substring(2));
				return String.format("%s%05d", LEAVE_APPLY_CODE_PREFIX, lastNumber + 1);
			}
		} catch (NumberFormatException e) {
			log.error("Failed to increment leave code", e);
		}
		return LEAVE_APPLY_INITIAL_CODE;
	}

	private String incrementLeaveTypeCode(String lastCode) {
		try {
			if (isValidCode(lastCode, LEAVE_TYPE_CATEGORY_PREFIX, LEAVE_TYPE_CATEGORY_LENGTH)) {
				int lastNumber = Integer.parseInt(lastCode.substring(3));
				return String.format("%s%03d", LEAVE_TYPE_CATEGORY_PREFIX, lastNumber + 1);
			}
		} catch (NumberFormatException e) {
			log.error("Failed to increment leave type code", e);
		}
		return LEAVE_TYPE_CATEGORY_INITIAL_CODE;
	}

	private boolean isValidCode(String code, String prefix, int length) {
		return code != null && code.startsWith(prefix) && code.length() == length
				&& code.substring(prefix.length()).chars().allMatch(Character::isDigit);
	}

	/**
	 * Generates the next sequential transaction ID for a specific LeaveMaster
	 * 
	 * @param leaveMaster The LeaveMaster document containing existing transactions
	 * @return The next transaction ID (e.g., T001, T002)
	 */
	public String generateTransactionId(LeaveMaster leaveMaster) {

		if (leaveMaster == null || leaveMaster.getLeaveTransactions() == null
				|| leaveMaster.getLeaveTransactions().isEmpty()) {
			return LEAVE_TRANSACTION_INITIAL_CODE;
		}

		int maxId = leaveMaster.getLeaveTransactions().stream().map(LeaveTransactions::getTransactionId)
				.filter(Objects::nonNull)
				.filter(id -> id.startsWith(LEAVE_TRANSACTION_PREFIX)
						&& id.length() > LEAVE_TRANSACTION_PREFIX.length())
				.map(id -> id.substring(LEAVE_TRANSACTION_PREFIX.length())).filter(numStr -> numStr.matches("\\d+"))
				.mapToInt(Integer::parseInt).max().orElse(0);

		int nextId = maxId + 1;
		return String.format("%s%03d", LEAVE_TRANSACTION_PREFIX, nextId);
	}
}
