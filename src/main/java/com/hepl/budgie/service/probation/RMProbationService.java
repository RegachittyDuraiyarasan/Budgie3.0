package com.hepl.budgie.service.probation;

import com.hepl.budgie.dto.probation.AddProbationDTO;
import com.hepl.budgie.dto.probation.FeedbackFormDTO;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.entity.probation.ProbationProcess;

import java.util.List;

public interface RMProbationService {

    List<ProbationFetchDTO> getCurrentProbation();

    ProbationProcess addFeedbackForm(String empId, AddProbationDTO addProbationDTO);

    List<ProbationFetchDTO> getExtendedProbation();

    List<ProbationFetchDTO> getConfirmedProbation();

    FeedbackFormDTO getFeedbackForm(String empId);
}
