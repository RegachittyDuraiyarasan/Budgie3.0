package com.hepl.budgie.controller.preonboarding;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.preonboarding.ItInfoEmailIdCreationDTO;
import com.hepl.budgie.dto.preonboarding.UpdateEmailRequestDTO;
import com.hepl.budgie.service.preonboarding.ItInfoEmailIdCreationService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itInfo")
@RequiredArgsConstructor
@Slf4j
public class ItInfoEmailIdCreationController {

    private final ItInfoEmailIdCreationService itInfoEmailIdCreationService;
    private final Translator translator;

    @GetMapping("/fetch")
    public GenericResponse<Object> fetchItInfoEmailId() {

        List<ItInfoEmailIdCreationDTO> itInfoEmailIdCreationDTOList = itInfoEmailIdCreationService.fetch();

        log.info("Fetched {} IT Info Email ID records", itInfoEmailIdCreationDTOList.size());
        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .data(itInfoEmailIdCreationDTOList)
                .build();
    }
    @PostMapping("/updateItInfraMail")
    public GenericResponse<Object> updateSuggestedEmails(@RequestBody List<UpdateEmailRequestDTO> requests) {
        try {
            log.info("Updating suggested emails for {} employees", requests.size());
            itInfoEmailIdCreationService.updateSuggestedEmails(requests);
            return GenericResponse.success(translator.toLocale(AppMessages.ONBOARDING_INFO));
        } catch (Exception e) {
            log.error("Error updating suggested emails", e);
            return GenericResponse.builder()
                    .status(false)
                    .message("Failed to update suggested emails.")
                    .errorType("SERVER_ERROR")
                    .build();
        }
    }
}
