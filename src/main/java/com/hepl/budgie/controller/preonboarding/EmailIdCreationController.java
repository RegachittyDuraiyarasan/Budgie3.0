package com.hepl.budgie.controller.preonboarding;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.preonboarding.EmailIdCreationDTO;
import com.hepl.budgie.dto.preonboarding.UpdateEmailRequestDTO;
import com.hepl.budgie.service.preonboarding.EmailIdCreationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
@Slf4j
public class EmailIdCreationController {

    private final EmailIdCreationService emailIdCreationService;
    private final Translator translator;

    @GetMapping()
    public GenericResponse<List<EmailIdCreationDTO>> fetchEmailDetailsByEmpIds() {

        List<EmailIdCreationDTO> emailDetails = emailIdCreationService.fetchByEmpIds();

        if (emailDetails.isEmpty()) {
            return GenericResponse.<List<EmailIdCreationDTO>>builder()
                    .status(false)
                    .message(translator.toLocale(AppMessages.NO_DATA_FOUND))
                    .errorType("NOT_FOUND")
                    .data(null)
                    .build();
        }

        return GenericResponse.<List<EmailIdCreationDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .data(emailDetails)
                .build();
    }
    @PostMapping("/update-suggested-email")
    public GenericResponse<String> updateSuggestedEmails(@RequestBody List<UpdateEmailRequestDTO> requests) {
        log.info("Updating suggested emails for {} employees", requests.size());

        emailIdCreationService.updateSuggestedEmails(requests);
      return GenericResponse.success(translator.toLocale(AppMessages.ONBOARDING_INFO));
    }
    @GetMapping("/fetchAllEmployees")
    public GenericResponse<List<EmailIdCreationDTO>> fetchAllEmployees() {

        List<EmailIdCreationDTO> emailDetails = emailIdCreationService.fetchAllEmpIds();

        if (emailDetails.isEmpty()) {
            return GenericResponse.<List<EmailIdCreationDTO>>builder()
                    .status(false)
                    .message(translator.toLocale(AppMessages.NO_DATA_FOUND))
                    .errorType("NOT_FOUND")
                    .data(null)
                    .build();
        }

        return GenericResponse.<List<EmailIdCreationDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .data(emailDetails)
                .build();
    }







}
