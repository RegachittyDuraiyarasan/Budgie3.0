package com.hepl.budgie.service.movement;

import com.hepl.budgie.dto.movement.*;

import java.util.List;
import java.util.Map;

public interface MovementService {
    List<EmpCodeValueDTO> getEmployeeCodeUnderRM(String empId);

    void initiateMovementByRM(MovementInitiateDTO request);

    List<MovementFetchDTO> getEmployeesForPrimaryReportingManager(String[] hrStatus, Boolean initializerWithdraw);

    Map<String, Object> extractHrAndWithdrawStatus(String hrStatus, String withdrawStatus);

    List<MovementFetchDTO> getEmployeesUnderReviewer(String teamType);

    void updateReviewerStatus(List<ReviewerUpdateDTO> requestList);

    void updateHRStatus(List<HrUpdateDTO> request);

    List<MovementFetchDTO> getMovementInfoByHrStatus(String hrStatus, String initializerWithdraw);

    void updateWithdrawStatus(String empId, String movementId);

    EmployeeCurrentDetail getEmployeeOldDetails(String empId);
}
