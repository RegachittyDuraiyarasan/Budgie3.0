package com.hepl.budgie.service.userinfo;

import com.hepl.budgie.dto.userinfo.EducationDTO;
import com.hepl.budgie.dto.userinfo.EducationRequestDTO;
import com.hepl.budgie.dto.userinfo.UpdateEducationDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EducationInfoService {

    void addEducation(String empId, EducationRequestDTO eduDetailsDTO, List<MultipartFile> files);

    List<EducationDTO> getEducation (String empId);

    void updateEducation(String empId,String educationId, UpdateEducationDTO educationRequest);

    void deleteEducation(String empId,String educationId);
}
