package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.DivisionHeadDTO;
import com.hepl.budgie.dto.userinfo.HRInfoDTO;
import com.hepl.budgie.dto.userinfo.PrimaryDTO;
import com.hepl.budgie.dto.userinfo.ReviewerDTO;
import com.hepl.budgie.entity.separation.HRInfo;
import com.hepl.budgie.entity.userinfo.*;
import com.hepl.budgie.mapper.userinfo.HRInfoMapper;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.userinfo.HRInformationService;

import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HRInformationServiceImplementation implements HRInformationService {

    private final MongoTemplate mongoTemplate;

    private final UserInfoRepository userInfoRepository;

    private final HRInfoMapper hrInfoMapper;

    private final JWTHelper jwtHelper;

    @Override
    public List<PrimaryDTO> getReportingManager() {
        return userInfoRepository.listAllPrimaryManagers(mongoTemplate).getMappedResults();
    }

    @Override
    public List<ReviewerDTO> getReviewer() {
        return userInfoRepository.listAllReviewer(mongoTemplate).getMappedResults();
    }

    @Override
    public List<DivisionHeadDTO> getDivisionHead() {
        return userInfoRepository.listAllDivisionHead(mongoTemplate).getMappedResults();
    }

    @Override
    public UserInfo updateHRInfo(String empId,FormRequest formRequest) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

        // Get existing HR information (preserving data)
        HrInformation existingHRInfo = (userInfo.getSections() != null && userInfo.getSections().getHrInformation() != null)
                ? userInfo.getSections().getHrInformation()
                : new HrInformation();

        // Extract only updated fields
        HrInformation updatedHRInfo = hrInfoMapper.toEntity(formRequest.getFormFields());

        // Update only required fields, explicitly preserving other fields
        existingHRInfo.setPrimary(updatedHRInfo.getPrimary() != null ? updatedHRInfo.getPrimary() : existingHRInfo.getPrimary());
        existingHRInfo.setReviewer(updatedHRInfo.getReviewer() != null ? updatedHRInfo.getReviewer() : existingHRInfo.getReviewer());
        existingHRInfo.setDivisionHead(updatedHRInfo.getDivisionHead() != null ? updatedHRInfo.getDivisionHead() : existingHRInfo.getDivisionHead());

        // Ensure other fields remain unchanged
        existingHRInfo.setSecondary(existingHRInfo.getSecondary());
        existingHRInfo.setRecruiter(existingHRInfo.getRecruiter());
        existingHRInfo.setOnboarder(existingHRInfo.getOnboarder());
        existingHRInfo.setBuddy(existingHRInfo.getBuddy());
        existingHRInfo.setNoticePeriod(existingHRInfo.getNoticePeriod());
        existingHRInfo.setAttendanceFormat(existingHRInfo.getAttendanceFormat());
        existingHRInfo.setWeekOff(existingHRInfo.getWeekOff());
        existingHRInfo.setLeaveScheme(existingHRInfo.getLeaveScheme());
        existingHRInfo.setOnboardingStatus(existingHRInfo.getOnboardingStatus());
        existingHRInfo.setInductionMailStatus(existingHRInfo.getInductionMailStatus());
        existingHRInfo.setBuddyMailStatus(existingHRInfo.getBuddyMailStatus());
        existingHRInfo.setSpocStatus(existingHRInfo.isSpocStatus());
        existingHRInfo.setSpocReportingManagerStatus(existingHRInfo.isSpocReportingManagerStatus());
        existingHRInfo.setDocumentVerificationStatus(existingHRInfo.getDocumentVerificationStatus());

        // Set the updated HR info back to the user
        Sections sections = userInfo.getSections() != null ? userInfo.getSections() : new Sections();
        sections.setHrInformation(existingHRInfo);
        userInfo.setSections(sections);

        userInfoRepository.save(userInfo);
        return userInfo;
    }



    @Override
    public HRInfoDTO getHRInfo(String empId) {

        // Check if employee exists in the database
        userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));

        // Fetch HR details using aggregation
        List<HRInfoDTO> results = userInfoRepository.getEmployeeManagerDetails(empId, mongoTemplate);

        // Return the first result if available
        return results.stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.HR_INFO_NOT_FOUND));
    }


}
