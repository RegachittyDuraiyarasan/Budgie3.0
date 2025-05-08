package com.hepl.budgie.service.preonboarding;

import com.hepl.budgie.dto.preonboarding.EmailIdCreationDTO;
import com.hepl.budgie.dto.preonboarding.UpdateEmailRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface EmailIdCreationService {
    List<EmailIdCreationDTO> fetchByEmpIds();
    void updateSuggestedEmails(List<UpdateEmailRequestDTO> requests);
    List<EmailIdCreationDTO>fetchAllEmpIds();
}