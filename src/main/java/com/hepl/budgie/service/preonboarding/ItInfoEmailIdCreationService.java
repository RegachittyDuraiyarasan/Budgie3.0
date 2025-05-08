package com.hepl.budgie.service.preonboarding;

import com.hepl.budgie.dto.preonboarding.ItInfoEmailIdCreationDTO;
import com.hepl.budgie.dto.preonboarding.UpdateEmailRequestDTO;

import java.util.List;


public interface ItInfoEmailIdCreationService {
    List<ItInfoEmailIdCreationDTO> fetch();
    void updateSuggestedEmails(List<UpdateEmailRequestDTO> requests);
}
