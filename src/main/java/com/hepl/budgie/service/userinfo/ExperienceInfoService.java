package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.userinfo.ExperienceDTO;
import com.hepl.budgie.dto.userinfo.ExperienceRequestDTO;
import com.hepl.budgie.dto.userinfo.UpdateExperienceDTO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ExperienceInfoService {

    void addExperience(String empId,ExperienceRequestDTO expDetailsDTO, List<MultipartFile> files);

    List<ExperienceDTO> getExperience(String empId);

    void updateExperience(String empId,String experienceId, UpdateExperienceDTO updateExperienceDTO);

    void deleteExperience(String empId,String experienceId);
}
