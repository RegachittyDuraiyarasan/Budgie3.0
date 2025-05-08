package com.hepl.budgie.service.impl.workflow;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.entity.userinfo.HrInformation;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.workflow.UnseenRoleTypeService;
import com.hepl.budgie.utils.AppMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class UnseenRoleTypeServiceImplementation implements UnseenRoleTypeService {
    private final UserInfoRepository userInfoRepository;
    private final Translator translator;

    public UnseenRoleTypeServiceImplementation(UserInfoRepository userInfoRepository, Translator translator) {
        this.userInfoRepository = userInfoRepository;
        this.translator = translator;
    }

    @Override
    public String getReporterDetail(String empId, String searchId) {
        // Fetch UserInfo by empId
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

        // Fetch HrInformation
        Sections sections = userInfo.getSections();
        if (sections == null || sections.getHrInformation() == null) {
            throw new CustomResponseStatusException(AppMessages.HR_INFO_NOT_FOUND,HttpStatus.NOT_FOUND, new String[]{empId});
        }
        HrInformation hrInformation = sections.getHrInformation();
        return findMatchingReporterDetail(hrInformation, searchId ,empId);
    }

    private String findMatchingReporterDetail(HrInformation hrInformation, String searchId, String empId) {
        if (hrInformation.getPrimary() != null && searchId.equals(hrInformation.getPrimary().getManagerId())) {
            return "Reporting Manager";
        }
        if (hrInformation.getReviewer() != null && searchId.equals(hrInformation.getReviewer().getManagerId())) {
            return "Reviewer";
        }

        throw new CustomResponseStatusException(AppMessages.REPORTER_DETAIL_NOT_FOUND,HttpStatus.NOT_FOUND, new String[]{empId});
    }


}
