package com.hepl.budgie.service.impl.separation;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.separation.EmployeeInfoDTO;
import com.hepl.budgie.dto.separation.EmployeeSeparationDTO;
import com.hepl.budgie.dto.separation.SeparationReportDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.separation.FinanceInfo;
import com.hepl.budgie.entity.separation.HRInfo;
import com.hepl.budgie.entity.separation.ITInfraInfo;
import com.hepl.budgie.entity.separation.Level;
import com.hepl.budgie.entity.separation.ReportingManagerInfo;
import com.hepl.budgie.entity.separation.ReviewerInfo;
import com.hepl.budgie.entity.separation.SeparationExitInfo;
import com.hepl.budgie.entity.separation.SeparationInfo;
import com.hepl.budgie.entity.separation.SiteAdminInfo;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.organization.OrganizationRepository;
import com.hepl.budgie.repository.separation.SeparationExitInfoRepository;
import com.hepl.budgie.repository.separation.SeparationRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.PdfService;
import com.hepl.budgie.service.TemplateService;
import com.hepl.budgie.service.separation.SeparationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SeparationServiceImp implements SeparationService {
    private final UserInfoRepository userInfoRepository;
    private final MongoTemplate mongoTemplate;
    private final SeparationRepository separationRepository;
    private final JWTHelper jwtHelper;
    private final SeparationExitInfoRepository separationExitInfoRepository;
    private final TemplateService templateService;
    private final PdfService pdfService;
    private final OrganizationRepository organizationRepository;

    @Override
    public EmployeeInfoDTO getEmployeeDetails(String empId) {
        log.info("getEmployeeDetails");
        UserRef userRef = jwtHelper.getUserRefDetail();
        return userInfoRepository.getEmployeeDetails(empId, userRef.getOrganizationCode(), mongoTemplate,
                LocaleContextHolder.getTimeZone());
    }

    @Override
    public EmployeeSeparationDTO updateOrInsertEmployeeSeparation(String org, EmployeeSeparationDTO dto) {
        log.info("updateOrInsertEmployeeSeparation");

        String currentUserId = jwtHelper.getUserRefDetail().getEmpId();
        LocalDateTime now = LocalDateTime.now();

        if (dto.getReportingManagerInfo() != null) {
            ReportingManagerInfo reportingManagerInfo = dto.getReportingManagerInfo();
            reportingManagerInfo.setApprovedOn(now);
            reportingManagerInfo.setApprovedBy(currentUserId);
            reportingManagerInfo.setStatus(Status.APPROVED.label);
        } else if (dto.getReviewerInfo() != null) {
            ReviewerInfo reviewerInfo = dto.getReviewerInfo();
            reviewerInfo.setReviewerId(currentUserId);
            reviewerInfo.setApprovedBy(currentUserId);
            reviewerInfo.setApprovedOn(now);
            reviewerInfo.setStatus(Status.APPROVED.label);
        } else if (dto.getItInfraInfo() != null) {
            ITInfraInfo itInfraInfo = dto.getItInfraInfo();
            itInfraInfo.setApprovedBy(currentUserId);
            itInfraInfo.setApprovedOn(now);
            itInfraInfo.setStatus(Status.APPROVED.label);
        } else if (dto.getFinanceInfo() != null) {
            FinanceInfo financeInfo = dto.getFinanceInfo();
            financeInfo.setApprovedBy(currentUserId);
            financeInfo.setApprovedOn(now);
            financeInfo.setStatus(Status.APPROVED.label);
        } else if (dto.getSiteAdminInfo() != null) {
            SiteAdminInfo siteAdminInfo = dto.getSiteAdminInfo();
            siteAdminInfo.setApprovedBy(currentUserId);
            siteAdminInfo.setApprovedOn(now);
            siteAdminInfo.setStatus(Status.APPROVED.label);
        } else if (dto.getHrInfo() != null) {
            HRInfo hrInfo = dto.getHrInfo();
            hrInfo.setApprovedBy(currentUserId);
            hrInfo.setApprovedOn(now);
            hrInfo.setStatus(Status.APPROVED.label);
        }

        return separationRepository.updateOrInsertEmployeeSeparation(org, dto, mongoTemplate);
    }

    @Override
    public List<EmployeeSeparationDTO> getSeparationData(String org, String empId, String level) {
        return separationRepository.getSeparationData(org, empId, mongoTemplate);
    }

    @Override
    public List<?> getSeparationDataByRepoAndReview(String level, String status) {
        UserRef userRef = jwtHelper.getUserRefDetail();
        List<?> employeeSeparationDTOs = null;
        if (level.equals(Level.ITINFRA.label) || level.equals(Level.FINANCE.label)
                || level.equals(Level.SITEADMIN.label) || level.equals(Level.ACCOUNT.label)) {
            employeeSeparationDTOs = getNoDueSeparationData(level, status);
        } else if (level.equals(Level.HR.label)) {
            employeeSeparationDTOs = getHROpsSeparationData(status);
        } else if (level.equals(Level.SEPARATIONREPORT.label)) {
            employeeSeparationDTOs = getHRSeparationReportData(status);
        } else {
            List<UserInfo> userInfo = null;
            if (Level.REPORTINGMANAGER.label.equals(level)) {
                userInfo = userInfoRepository.getEmployeesByCriteria(userRef.getEmpId(),
                        "sections.hrInformation.primary.managerId", mongoTemplate);
            } else if (Level.REVIEWER.label.equals(level)) {
                userInfo = userInfoRepository.getEmployeesByCriteria(userRef.getEmpId(),
                        "sections.hrInformation.reviewer.managerId", mongoTemplate);
            }
            employeeSeparationDTOs = separationRepository.getSeparationDataByRepoAndReview(
                    userRef.getOrganizationCode(), userRef.getEmpId(), status, level, userInfo, mongoTemplate);
        }
        return employeeSeparationDTOs;
    }

    private List<EmployeeSeparationDTO> getNoDueSeparationData(String level, String status) {
        UserRef userRef = jwtHelper.getUserRefDetail();
        List<EmployeeSeparationDTO> employeeSeparationDTOs = separationRepository
                .getITInfraSeparationData(userRef.getOrganizationCode(), level, status, mongoTemplate);
        return employeeSeparationDTOs;
    }

    private List<EmployeeSeparationDTO> getHROpsSeparationData(String status) {
        UserRef userRef = jwtHelper.getUserRefDetail();

        List<EmployeeSeparationDTO> employeeSeparationDTOs = separationRepository
                .getHROpsSeparationData(userRef.getOrganizationCode(), status, mongoTemplate,
                        LocaleContextHolder.getTimeZone());
        return employeeSeparationDTOs;
    }

    private List<SeparationReportDTO> getHRSeparationReportData(String status) {
        UserRef userRef = jwtHelper.getUserRefDetail();
        List<SeparationReportDTO> employeeSeparationDTOs = separationRepository
                .getHRSeparationReportData(userRef.getOrganizationCode(), status, mongoTemplate);
        return employeeSeparationDTOs;
    }

    @Override
    public SeparationExitInfo upsertSeparationExitInfo(SeparationExitInfo separationExitInfo) {
        UserRef userRef = jwtHelper.getUserRefDetail();
        return separationExitInfoRepository.upsertSeparationExitInfo(userRef.getOrganizationCode(), separationExitInfo,
                mongoTemplate);
    }

    @Override
    public SeparationExitInfo getSeparationExitInfoBySeparationId(String separationId) {
        UserRef userRef = jwtHelper.getUserRefDetail();
        return separationExitInfoRepository.getSeparationExitInfoBySeparationId(userRef.getOrganizationCode(),
                separationId, mongoTemplate);
    }

    public byte[] generateRelievingLetter(String empId) throws IOException{
        EmployeeInfoDTO empInfoDTO = getEmployeeDetails(empId);
        LocalDate date = LocalDate.now();
        byte[] pdf = pdfService.generatePdf(templateService.getRelievingLetter(date.toString(), empInfoDTO.getEmpId(), empInfoDTO.getDesignation(), empInfoDTO.getFirstName()+" "+empInfoDTO.getMiddleName(),
                empInfoDTO.getRelievingDate().toString(), empInfoDTO.getDateOfJoining().toString(), empId));
        return pdf;
    }
    @Override
    public List<SeparationInfo> getUpcomingRelieving(){
        List<String> orgcods = organizationRepository.getOrganizationCodes(mongoTemplate);
        List<SeparationInfo>  infos = separationRepository.getUpcomingRelieving(orgcods, mongoTemplate);
        return infos;
    }
    @Scheduled(cron = "0 02 18 * * ?")
    void updateUserInfoForUpcomingRelieving(){
        List<String> orgCodes = organizationRepository.getOrganizationCodes(mongoTemplate);
        List<SeparationInfo> separationInfos = separationRepository.getUpcomingRelieving(orgCodes, mongoTemplate);
        userInfoRepository.updateUserInfoForUpcomingRelieving(separationInfos, mongoTemplate);
    }



}
