package com.hepl.budgie.controller.preonboarding;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.preonboarding.SeatingRequestDTO;
import com.hepl.budgie.service.preonboarding.OnBoardingSeatingAndIdCardService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/seating")
@RequiredArgsConstructor
public class OnBoardingSeatingAndIdCardController {
    private final OnBoardingSeatingAndIdCardService onBoardingSeatingAndIdCardService;
    private final Translator translator;

    @GetMapping("/pending")
    public GenericResponse<List<SeatingRequestDTO>> fetchSeating() {
        List<SeatingRequestDTO> seating = onBoardingSeatingAndIdCardService.getByOnboardingStatus();

        return GenericResponse.<List<SeatingRequestDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_STATUS_FETCH))
                .errorType("NONE")
                .data(seating)
                .build();
    }
    @GetMapping("/approved")
    public GenericResponse<List<SeatingRequestDTO>> fetch() {
        List<SeatingRequestDTO> seating = onBoardingSeatingAndIdCardService.getByOnboardingStatusTrue();

        return GenericResponse.<List<SeatingRequestDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_STATUS_FETCH))
                .errorType("NONE")
                .data(seating)
                .build();
    }
}
