package com.hepl.budgie.service.probation;

import com.hepl.budgie.dto.probation.AddProbationDTO;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.entity.probation.ProbationProcess;

import java.util.List;

public interface HRProbationService {
    List<ProbationFetchDTO> getCurrentHRProbation();

    List<ProbationFetchDTO> getExtendedHRProbation();

    List<ProbationFetchDTO> getConfirmedHRProbation();

    List<ProbationFetchDTO> getDeemedProbation();

    void updateHRVerifyStatuses(String empId,ProbationProcess request);
}
