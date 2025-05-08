package com.hepl.budgie.service.separation;

import java.io.IOException;
import java.util.List;

import com.hepl.budgie.dto.separation.EmployeeInfoDTO;
import com.hepl.budgie.dto.separation.EmployeeSeparationDTO;
import com.hepl.budgie.entity.separation.SeparationExitInfo;
import com.hepl.budgie.entity.separation.SeparationInfo;

public interface SeparationService {
    public EmployeeInfoDTO getEmployeeDetails(String empId);

    public EmployeeSeparationDTO updateOrInsertEmployeeSeparation(String org, EmployeeSeparationDTO dto);

    public List<EmployeeSeparationDTO> getSeparationData(String org, String empId, String level);

    public List<?> getSeparationDataByRepoAndReview(String level,String status);

    public SeparationExitInfo upsertSeparationExitInfo( SeparationExitInfo separationExitInfo);

    public SeparationExitInfo getSeparationExitInfoBySeparationId( String separationId);

    public byte[] generateRelievingLetter(String empId) throws IOException;

    public List<SeparationInfo> getUpcomingRelieving();

}
