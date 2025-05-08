package com.hepl.budgie.service.preonboarding;

import com.hepl.budgie.dto.preonboarding.SeatingRequestDTO;

import java.util.List;

public interface OnBoardingSeatingAndIdCardService {
    List<SeatingRequestDTO>getByOnboardingStatus();
    List<SeatingRequestDTO>getByOnboardingStatusTrue();

}
