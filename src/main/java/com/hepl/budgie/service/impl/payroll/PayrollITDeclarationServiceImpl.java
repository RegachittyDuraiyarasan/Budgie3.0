package com.hepl.budgie.service.impl.payroll;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.HraDTO;
import com.hepl.budgie.dto.payroll.LetOutDTO;
import com.hepl.budgie.dto.payroll.PayrollDeclarationDTO;
import com.hepl.budgie.dto.payroll.PayrollRequestDTO;
import com.hepl.budgie.dto.payroll.PayrollTypeDTO;
import com.hepl.budgie.dto.payroll.PreviousEmploymentTaxDTO;
import com.hepl.budgie.dto.payroll.SchemeUpdateDTO;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.Sequence;
import com.hepl.budgie.entity.payroll.FamilyList;
import com.hepl.budgie.entity.payroll.PayrollITDeclaration;
import com.hepl.budgie.entity.payroll.PayrollITScheme;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.organization.OrganizationRepository;
import com.hepl.budgie.repository.payroll.PayrollITDeclarationRepository;
import com.hepl.budgie.repository.payroll.PayrollITSchemeRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.payroll.PayrollITDeclarationService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollITDeclarationServiceImpl implements PayrollITDeclarationService {

    private final PayrollITDeclarationRepository payrollITDeclarationRepository;
    private final JWTHelper jwtHelper;
    private final UserInfoRepository userInfoRepository;
    private final MongoTemplate mongoTemplate;
    private final OrganizationRepository organizationRepository;
    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final PayrollITSchemeRepository payrollITSchemeRepository;

    @Override
    public List<Map<String, String>> getEmployeesByPayrollRoleType() {

        log.info("fetch Employees by role type: {}");
        String orgId = jwtHelper.getOrganizationCode();
        List<Organization> orgs = organizationRepository.findByOrgIdAndIt(mongoTemplate, orgId);

        List<String> roleTypes = new ArrayList<>();
        for (Organization org : orgs) {
            if (org.getSequence() != null) {
                for (Sequence seq : org.getSequence()) {
                    if (seq.getRoleType() != null) {
                        roleTypes.add(seq.getRoleType());
                    }
                }
            }
        }
        List<UserInfo> users = userInfoRepository.findUsersByRoleType(roleTypes, orgId, mongoTemplate, orgId);
        List<String> empIds = users.stream().map(UserInfo::getEmpId).collect(Collectors.toList());
        Set<String> existingEmpIds = payrollITDeclarationRepository.findExistingEmpIds(empIds, orgId, mongoTemplate);
        return users.stream()
                .filter(user -> !existingEmpIds.contains(user.getEmpId()))
                .map(user -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("empId", user.getEmpId());
                    map.put("empName", user.getSections().getBasicDetails().getFirstName() + " "
                            + user.getSections().getBasicDetails().getLastName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<PayrollITDeclaration> releasePayrollITDeclaration(PayrollRequestDTO release) {

        log.info("Release Payroll IT Declaration: {}");
        String orgId = jwtHelper.getOrganizationCode();
        PayrollLockMonth payrollLockMonth = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, orgId,
                "IN");
        String finYear = payrollLockMonth.getFinYear();
        String planId = generateUniquePlanId(orgId);
        return payrollITDeclarationRepository.saveItDeclarationRelease(release, orgId, finYear, planId, mongoTemplate);
    }

    public String generateUniquePlanId(String orgId) {

        PayrollITDeclaration lastRecord = payrollITDeclarationRepository.findLastPlanId(orgId, mongoTemplate);
        String newPlanId = "P00001";
        if (lastRecord != null && lastRecord.getPlanId() != null) {
            String lastPlanId = lastRecord.getPlanId();
            String numericPart = lastPlanId.substring(1);
            int nextNumber = Integer.parseInt(numericPart) + 1;
            newPlanId = "P" + String.format("%05d", nextNumber);
        }
        return newPlanId;
    }

    @Override
    public List<PayrollITDeclaration> getPayrollReReleaseList() {

        log.info("Get Payroll IT Declaration: {}");
        String orgId = jwtHelper.getOrganizationCode();
        return payrollITDeclarationRepository.findByStatus(orgId, mongoTemplate);
    }

    @Override
    public List<PayrollITDeclaration> reReleasePayrollITDeclaration(PayrollRequestDTO release) {

        log.info("Re-Release Payroll IT Declaration: {}");
        String orgId = jwtHelper.getOrganizationCode();
        PayrollLockMonth payrollLockMonth = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, orgId,
                "IN");
        String finYear = payrollLockMonth.getFinYear();
        return payrollITDeclarationRepository.saveItDeclarationReRelease(release, orgId, finYear, mongoTemplate);
    }

    @Override
    public PayrollDeclarationDTO getEmployeePayrollDeclaration() {

        log.info("Get Employee Payroll Declaration: {}");
        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        PayrollITDeclaration payrollITDeclaration = payrollITDeclarationRepository.findByEmpId(empId, orgId,
                mongoTemplate);
        ZonedDateTime endDate = payrollITDeclaration.getEndDate();
        if (endDate != null && endDate.isBefore(ZonedDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.IT_DECLARATION_PAST_DATE);
        }
        PayrollDeclarationDTO payrollDeclarationDTO = new PayrollDeclarationDTO();
        if (payrollITDeclaration.getRegime() == null) {
            payrollDeclarationDTO.setShowDeclaration(false);
            payrollDeclarationDTO.setShowModel(false);
            payrollDeclarationDTO.setMessage(null);
            payrollDeclarationDTO.setSchemes(null);
            return payrollDeclarationDTO;
        }
        if (payrollITDeclaration.getRegime().equalsIgnoreCase("New Regime")) {
            payrollDeclarationDTO.setShowDeclaration(false);
            payrollDeclarationDTO.setShowModel(false);
            payrollDeclarationDTO.setMessage("You're new regime, so you can't access");
            payrollDeclarationDTO.setSchemes(null);
            return payrollDeclarationDTO;
        }

        if (payrollITDeclaration.getRegime().equalsIgnoreCase("Old Regime")) {
            String status = payrollITDeclaration.getStatus();
            if (DataOperations.CREATED.label.equalsIgnoreCase(status)
                    || DataOperations.DRAFT.label.equalsIgnoreCase(status)) {
                payrollDeclarationDTO.setShowDeclaration(true);
            } else {
                payrollDeclarationDTO.setShowDeclaration(false);
            }
            payrollDeclarationDTO.setShowModel(false);
            payrollDeclarationDTO.setMessage(null);
            List<PayrollTypeDTO> payrollType = payrollITSchemeRepository.getPayrollType(mongoTemplate, orgId, "IN");
            payrollDeclarationDTO.setSchemes(payrollType);
            payrollDeclarationDTO.setPreviousEmploymentTax(payrollITDeclaration.getPreviousEmployeeTax());
            return payrollDeclarationDTO;
        }
        return payrollDeclarationDTO;
    }

    @Override
    public PayrollITDeclaration updateRegime(String regime) {

        log.info("Update Payroll IT Declaration Regime: {}");
        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        return payrollITDeclarationRepository.updateRegime(orgId, empId, regime, mongoTemplate);
    }

    @Override
    public PayrollITDeclaration updateSchemes(SchemeUpdateDTO schemes) {

        log.info("update schemes in Payroll IT declaration");
        String orgId = jwtHelper.getOrganizationCode();
        String type = schemes.getType();
        PayrollITScheme itSchemes = payrollITSchemeRepository.findByType(type, orgId, mongoTemplate, type);
        return payrollITDeclarationRepository.updateSchemes(schemes, itSchemes, orgId, mongoTemplate);
    }

    @Override
    public PayrollITDeclaration updateHra(String planId, HraDTO hra) {

        log.info("update Hra in Payroll IT declaration");
        String orgId = jwtHelper.getOrganizationCode();
        return payrollITDeclarationRepository.updateHra(planId, hra, orgId, mongoTemplate);
    }

    @Override
    public PayrollITDeclaration updateLetOut(String planId, LetOutDTO itLetOut) {

        log.info("update LetOut in Payroll IT declaration");
        String orgId = jwtHelper.getOrganizationCode();
        return payrollITDeclarationRepository.updateLetOut(planId, itLetOut, orgId, mongoTemplate);
    }

    @Override
    public PayrollITDeclaration updateMetro(String planId, String metro) {

        log.info("update Metro in Payroll IT declaration");
        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        userInfoRepository.updateMetro(empId, mongoTemplate, metro);
        return payrollITDeclarationRepository.updateMetro(metro, planId, empId, orgId, mongoTemplate);
    }

    @Override
    public PayrollITDeclaration updatePreviousEmployee(String planId,
            PreviousEmploymentTaxDTO employeeTax) {

        log.info("update Previous Employee in Payroll IT declaration");
        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        AppMessages.USER_NOT_FOUND));
        LocalDate date = LocalDate.now();
        String currentMonth = date.getMonthValue() + "-" + date.getYear();
        String finYear = getFinancialYear(currentMonth);
        LocalDate doj = userInfo.getSections().getWorkingInformation().getDoj().toLocalDate();
        String[] years = finYear.split("-");
        LocalDate fyStart = LocalDate.of(Integer.parseInt(years[0]), 4, 1);
        LocalDate fyEnd = LocalDate.of(Integer.parseInt(years[1]), 3, 31); 

        if (doj.isBefore(fyStart) || doj.isAfter(fyEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.NOT_ELIGIBLE_FOR_PREVIOUS_EMPLOYEE);
        }
        return payrollITDeclarationRepository.updatePreviousEmployee(planId, employeeTax, orgId, mongoTemplate);
    }

    @Override
    public PayrollITDeclaration draftPreviousEmployee(String planId, PreviousEmploymentTaxDTO employeeTax) {

        log.info("update Previous Employee in Payroll IT declaration");
        String orgId = jwtHelper.getOrganizationCode();
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        AppMessages.USER_NOT_FOUND));
        LocalDate date = LocalDate.now();
        String currentMonth = date.getMonthValue() + "-" + date.getYear();
        String finYear = getFinancialYear(currentMonth);
        LocalDate doj = userInfo.getSections().getWorkingInformation().getDoj().toLocalDate();
        String[] years = finYear.split("-");
        LocalDate fyStart = LocalDate.of(Integer.parseInt(years[0]), 4, 1);
        LocalDate fyEnd = LocalDate.of(Integer.parseInt(years[1]), 3, 31); 
        if (doj.isBefore(fyStart) || doj.isAfter(fyEnd)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.NOT_ELIGIBLE_FOR_PREVIOUS_EMPLOYEE);
        }
        return payrollITDeclarationRepository.updateDraftPreviousEmployee(planId, employeeTax, orgId, mongoTemplate);
    }

    public String getFinancialYear(String currentMonth) {
        int month = Integer.parseInt(currentMonth.split("-")[0]);
        int year = Integer.parseInt(currentMonth.split("-")[1]);

        if (month >= 4) {
            return year + "-" + (year + 1);
        } else {
            return (year - 1) + "-" + year;
        }
    }

    @Override
    public PayrollITDeclaration updateFamilies(String planId, List<FamilyList> families) {
        
        log.info("update Families in Payroll IT declaration");
        String orgId = jwtHelper.getOrganizationCode();
        List<FamilyList> familyList = new ArrayList<>();
        for (FamilyList family : families) {
            FamilyList familyDetails = new FamilyList(family.getName(), family.getRelation(), family.getDob(),
                    family.getAge());
            familyList.add(familyDetails);
        }
        return payrollITDeclarationRepository.updateFamilies(planId, familyList, orgId, mongoTemplate);
    }
}
