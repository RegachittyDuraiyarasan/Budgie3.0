package com.hepl.budgie.controller.probation;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.probation.ProbationFetchDTO;
import com.hepl.budgie.entity.probation.ProbationProcess;
import com.hepl.budgie.service.probation.HRProbationService;
import com.hepl.budgie.service.probation.RMProbationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/HR-probation")
public class HRProbationController {

    private final HRProbationService hrProbationService;

    private final Translator translator;

    @GetMapping("/currentHRProbation")
    public GenericResponse<List<ProbationFetchDTO>> getCurrentHRProbation (){
        List<ProbationFetchDTO> currentHRProbation = hrProbationService.getCurrentHRProbation();
        return GenericResponse.success(currentHRProbation);
    }

    @GetMapping("/extendedHRProbation")
    public GenericResponse<List<ProbationFetchDTO>> getExtendedHRProbation (){
        List<ProbationFetchDTO> extendedHRProbation = hrProbationService.getExtendedHRProbation();
        return GenericResponse.success(extendedHRProbation);
    }

    @GetMapping("/confirmedHRProbation")
    public GenericResponse<List<ProbationFetchDTO>> getConfirmedHRProbation (){
        List<ProbationFetchDTO> confirmedHRProbation = hrProbationService.getConfirmedHRProbation();
        return GenericResponse.success(confirmedHRProbation);
    }

    @GetMapping("/deemedProbation")
    public GenericResponse<List<ProbationFetchDTO>> getDeemedProbation (){
        List<ProbationFetchDTO> deemedProbation = hrProbationService.getDeemedProbation();
        return GenericResponse.success(deemedProbation);
    }

    @PutMapping("/{empId}")
    public GenericResponse<String> updateHRVerifyStatuses(@PathVariable String empId, @RequestBody ProbationProcess request){
       hrProbationService.updateHRVerifyStatuses(empId,request);
        return GenericResponse.success(translator.toLocale(AppMessages.HR_STATUS));
    }
}
