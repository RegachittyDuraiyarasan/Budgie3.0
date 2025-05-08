package com.hepl.budgie.controller.probation;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.probation.AddProbationDTO;
import com.hepl.budgie.dto.probation.FeedbackFormDTO;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.service.probation.RMProbationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/RM-probation")
@Slf4j
public class RMProbationController {

    private final RMProbationService probationService;

    private final Translator translator;

    @GetMapping("/currentProbation")
    public GenericResponse<List<ProbationFetchDTO>> getCurrentProbation (){
        List<ProbationFetchDTO> currentProbation = probationService.getCurrentProbation();
        return GenericResponse.success(currentProbation);
    }

    @PostMapping("/{empId}")
    public GenericResponse<String> addFeedback(@PathVariable String empId,@RequestBody AddProbationDTO addProbationDTO){
        probationService.addFeedbackForm(empId,addProbationDTO);
        String message = "Submitted".equalsIgnoreCase(addProbationDTO.getStatus()) ?
                translator.toLocale(AppMessages.FEEDBACK_FORM_SUBMITTED) : translator.toLocale(AppMessages.FEEDBACK_FORM_SAVED);
        return GenericResponse.success(message);
    }

    @GetMapping("/extendedProbation")
    public GenericResponse<List<ProbationFetchDTO>> getExtendedProbation (){
        List<ProbationFetchDTO> extendedProbation = probationService.getExtendedProbation();
        return GenericResponse.success(extendedProbation);
    }

    @GetMapping("/confirmedProbation")
    public GenericResponse<List<ProbationFetchDTO>> getConfirmedProbation (){
        List<ProbationFetchDTO> confirmedProbation = probationService.getConfirmedProbation();
        return GenericResponse.success(confirmedProbation);
    }

    @GetMapping("/{empId}")
    public GenericResponse<FeedbackFormDTO> getFeedbackForm(@PathVariable String empId){
        FeedbackFormDTO feedbackFormDTO = probationService.getFeedbackForm(empId);
        log.info("feedbackFormDTO {} ",feedbackFormDTO);
        return GenericResponse.success(feedbackFormDTO);
    }
}
