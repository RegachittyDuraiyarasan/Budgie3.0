package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.employee.EmployeeOrganisationDTO;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.role.EditRoleDTO;
import com.hepl.budgie.dto.userinfo.ImageDTO;
import com.hepl.budgie.dto.userinfo.PasswordDTO;
import com.hepl.budgie.dto.userinfo.ProfileDetailsDTO;
import com.hepl.budgie.entity.ExcelType;
import com.hepl.budgie.entity.FilePathStruct;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.repository.organization.OrganizationRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import com.hepl.budgie.utils.Base64Util;
import com.mongodb.client.result.UpdateResult;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import com.hepl.budgie.dto.employee.EmployeeDetailsDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.userinfo.*;
import com.hepl.budgie.mapper.userinfo.*;
import com.hepl.budgie.repository.master.MasterSettingsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.userinfo.EmployeeListService;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeListServiceImpl implements EmployeeListService {

    private final UserInfoRepository userInfoRepository;
    private final BasicDetailsMapper basicDetailsMapper;
    private final HRInfoMapper hrInfoMapper;
    private final ContactMapper contactMapper;
    private final WorkingInformationMapper workingInformationMapper;
    private final MasterSettingsRepository masterSettingsRepository;

    private final OrganizationRepository organizationRepository;
    private final Map<String, ExcelExport> excelExport;
    private static int tempIdCounter = 1;
    private final MongoTemplate mongoTemplate;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final JWTHelper jwtHelper;
    private final FileService fileService;
    private final Translator translator;

    @Override
    public void updateEmployeeRoleType(String employeeId, List<EditRoleDTO> roleDTOList) {
        if (roleDTOList == null || roleDTOList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role list cannot be empty.");
        }
        UserInfo userInfo = mongoTemplate.findOne(
                new Query(Criteria.where("empId").is(employeeId)),
                UserInfo.class);

        if (userInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found for empId: " + employeeId);
        }

        String payrollStatusName = userInfo.getSections().getWorkingInformation().getPayrollStatus(); // Adjust if field
                                                                                                      // path is
                                                                                                      // different

        for (EditRoleDTO dto : roleDTOList) {
            if (dto.getRoleTypes() == null || dto.getRoleTypes().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "RoleTypes cannot be empty.");
            }

            if (dto.getOrganizationCode().equals(payrollStatusName) &&
                    !dto.getRoleTypes().contains("Employee")) {
                throw new CustomResponseStatusException(AppMessages.EMP_ROLE_MUST, HttpStatus.BAD_REQUEST,
                        new String[] { payrollStatusName });
            }

            Query query = new Query(Criteria.where("empId").is(employeeId)
                    .and("subOrganization.organizationCode").is(dto.getOrganizationCode()));

            Update update = new Update()
                    .set("subOrganization.$.roleDetails", dto.getRoleTypes());

            UpdateResult result = mongoTemplate.updateFirst(query, update, UserInfo.class);
            if (result.getMatchedCount() == 0) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.NOT_FOUND_SUB_ORG);
            }
        }
    }

    @Override
    public void updateEmployeeOrganizations(String employeeId, String organization, List<String> subOrganization) {
        UserInfo userInfo = userInfoRepository.findByEmpId(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
        log.info("userInfo {}", userInfo);

        List<OrganizationRef> existingSubOrgs = userInfo.getSubOrganization();
        if (existingSubOrgs == null) {
            existingSubOrgs = new ArrayList<>();
        }

        List<OrganizationRef> parentOrg = organizationRepository
                .getOrganisationReference(mongoTemplate, List.of(organization)).getMappedResults();
        List<OrganizationRef> newSubOrgs = organizationRepository
                .getOrganisationReference(mongoTemplate, subOrganization).getMappedResults();

        existingSubOrgs = existingSubOrgs.stream()
                .filter(existingOrg -> subOrganization.contains(existingOrg.getOrganizationCode()))
                .collect(Collectors.toList());

        List<OrganizationRef> finalExistingSubOrgs = existingSubOrgs;
        List<OrganizationRef> filteredSubOrgs = newSubOrgs.stream()
                .filter(newOrg -> finalExistingSubOrgs.stream()
                        .noneMatch(
                                existingOrg -> existingOrg.getOrganizationCode().equals(newOrg.getOrganizationCode())))
                .collect(Collectors.toList());

        existingSubOrgs.addAll(filteredSubOrgs);

        userInfo.setOrganization(parentOrg.get(0)); // Update parent org
        userInfo.setSubOrganization(existingSubOrgs); // Update sub-orgs
        userInfoRepository.save(userInfo);
    }

    @Override
    public UserInfo addEmployee(FormRequest formRequest, String org) {
        String tempId = generateTempId();
        UserInfo userInfo = new UserInfo();
        String newEmpId = findMaxDevActionStatus();
        userInfo.setTempId(newEmpId);
        userInfo.setEmpId(newEmpId);
        userInfo.setStatus(Status.ACTIVE.label);

        Map<String, Object> formFields = formRequest.getFormFields();
        String organization = (String) formFields.get("organization");
        @SuppressWarnings("unchecked")
        String subOrganization = (String) formFields.get("subOrganization");

        List<OrganizationRef> parentOrg = organizationRepository
                .getOrganisationReference(mongoTemplate, List.of(organization)).getMappedResults();
        List<OrganizationRef> childOrg = organizationRepository
                .getOrganisationReference(mongoTemplate, List.of(subOrganization)).getMappedResults();

        if (parentOrg.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PARENT_ORG_NOT_EXIST);
        }
        if (childOrg.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.MULTI_CHILD_ORG_NOT_EXIST);
        }

        userInfo.setOrganization(parentOrg.get(0));
        userInfo.setSubOrganization(childOrg);

        Object accessTypes = formFields.get("accessType");
        if (accessTypes == null || !(accessTypes instanceof List)
                || !((List<String>) accessTypes).contains("Employee")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EMPLOYEE_ACCESS_TYPE);
        }
        childOrg.get(0).setRoleDetails((List<String>) accessTypes);

        // userInfo.setRoleDetails((List<String>) accessTypes);

        String grade = (String) formFields.get("grade");
        AggregationResults<MasterFormOptions> masterFormOptionResults = masterSettingsRepository.findByGrade(grade,
                mongoTemplate, org);

        MasterFormOptions option = Optional.ofNullable(masterFormOptionResults.getUniqueMappedResult())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

        ProbationSettings probationDetailConfig = Optional.ofNullable(option.getProbationDetail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Probation details not found"));

        String dojString = (String) formFields.get("doj");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate dojDate = LocalDate.parse(dojString, formatter);

        int probationMonths = probationDetailConfig.getDefaultDurationMonths();

        ProbationDetails probationDetails = new ProbationDetails();
        probationDetails.setProbation(probationDetailConfig.isProbationRequired());
        probationDetails.setInitialDurationMonths(probationMonths);
        probationDetails.setExtensionOptionsMonths(probationDetailConfig.getExtensionOptionsMonths());

        probationDetails.setProbationStartDate(dojDate.atStartOfDay(ZoneId.systemDefault()));
        probationDetails.setProbationEndDate(dojDate.plusMonths(probationMonths).atStartOfDay(ZoneId.systemDefault()));

        Sections sections = new Sections();

        sections.setProfileOverallSubmit("pending");
        sections.setBasicDetails(basicDetailsMapper.toEntity(formFields));
        sections.setContact(contactMapper.toEntity(formFields));

        HrInformation hrInformation = hrInfoMapper.toEntity(formFields);
        hrInformation.setNoticePeriod(option.getNoticePeriod());
        sections.setHrInformation(hrInformation);

        WorkingInformation workingInformation = workingInformationMapper.toEntity(formFields);
        Object ctcObject = formFields.get("ctc");
        long ctcLong;

        ctcLong = ((Integer) ctcObject).longValue();
        String ctcString = String.valueOf(ctcLong);
        String encryptedCtc = Base64Util.encode(ctcString);
        workingInformation.setCtc(encryptedCtc);

        double monthlyAmount = Math.ceil(ctcLong / 12.0);
        String encryptedCmt = Base64Util.encode(String.valueOf((int) monthlyAmount));
        workingInformation.setCmt(encryptedCmt);

        sections.setWorkingInformation(workingInformation);
        if (subOrganization != null && !subOrganization.isEmpty()) {
            workingInformation.setPayrollStatus(subOrganization);
            workingInformation.setPayrollStatusName(childOrg.get(0).getOrganizationDetail());
        }
        sections.setProbationDetails(probationDetails);

        String password = RandomStringUtils.randomAlphanumeric(8);
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = passwordEncoder.encode(password);
        userInfo.setPassword("$2a$10$VXWFdtCVdiG8U7yb2Dqooe6FX3cONEJNBMjZIof5faXcvZXaVxyBe");
        // userInfo.setPassword(hashedPassword);
        userInfo.setSections(sections);

        UserInfo savedUserInfo = userInfoRepository.save(userInfo);
        // try {
        // sendWelcomeEmail(savedUserInfo, password);
        // } catch (Exception e) {
        // log.error("Failed to send welcome email: {}", e.getMessage(), e);
        // }

        return savedUserInfo;
    }

    private void sendWelcomeEmail(UserInfo employee, String password) {
        String toEmail = employee.getSections().getContact().getPersonalEmailId();
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No personal email ID found for employee {}", employee.getEmpId());
            return;
        }

        String subject = "Welcome Onboard || Budgie";
        Context context = new Context();
        context.setVariable("firstname", employee.getSections().getBasicDetails().getFirstName());
        context.setVariable("empId", employee.getEmpId());
        context.setVariable("password", password);
        String htmlContent = templateEngine.process("EmpIdGenerated", context);
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Override
    public List<EmployeeDetailsDTO> findAllEmployees() {
        List<UserInfo> userInfos = userInfoRepository.findAll();
        return userInfos.stream()
                .map(this::mapToEmployeeDetailsDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeActiveDTO> findByStatus() {
        List<UserInfo> userInfoList = userInfoRepository.findByStatus("Active");
        if (userInfoList.isEmpty()) {
            return Collections.emptyList();
        }
        List<EmployeeActiveDTO> employeeActiveDTOList = new ArrayList<>();
        for (UserInfo userInfo : userInfoList) {
            if (userInfo.getSections() != null && userInfo.getSections().getBasicDetails() != null) {
                EmployeeActiveDTO dto = new EmployeeActiveDTO();
                dto.setEmployeeName(
                        userInfo.getSections().getBasicDetails().getFirstName() + " "
                                + userInfo.getSections().getBasicDetails().getLastName() + "-" + userInfo.getEmpId());
                dto.setEmpId(userInfo.getEmpId());
                employeeActiveDTOList.add(dto);
            }
        }
        return employeeActiveDTOList;
    }

    @Override
    public List<Map<String, String>> getGender(String referenceName) {
        MasterFormSettings masterForm = masterSettingsRepository.findByReferenceName(referenceName, mongoTemplate, jwtHelper.getOrganizationCode());
        List<Map<String, String>> genderList = new ArrayList<>();
        if (masterForm != null && masterForm.getOptions() != null) {
            for (MasterFormOptions option : masterForm.getOptions()) {
                Map<String, String> genderMap = new HashMap<>();
                genderMap.put("value", option.getName());
                genderMap.put("name", option.getName());
                genderList.add(genderMap);
            }
        }
        return genderList;
    }

    private EmployeeDetailsDTO mapToEmployeeDetailsDTO(UserInfo userInfo) {
        String zoneId = LocaleContextHolder.getTimeZone().getID();
        return new EmployeeDetailsDTO(
                userInfo.getEmpId(),
                getEmpName(userInfo),
                userInfo.getStatus(),
                getGender(userInfo),
                AppUtils.parseZonedDate("dd-MM-yyyy", userInfo.getSections().getWorkingInformation().getDoj(), zoneId),
                AppUtils.parseZonedDate("dd-MM-yyyy", userInfo.getSections().getBasicDetails().getDob(), zoneId),
                userInfo.getSections().getWorkingInformation().getDepartment(),
                userInfo.getSections().getWorkingInformation().getDesignation(),
                userInfo.getSections().getWorkingInformation().getWorkLocation(),
                userInfo.getSections().getWorkingInformation().getGrade(),
                userInfo.getSections().getWorkingInformation().getOfficialEmail(),
                userInfo.getSections().getContact().getPersonalEmailId(),
                userInfo.getSections().getContact().getPrimaryContactNumber(),
                getReviewerName(userInfo),
                getReportingManagerName(userInfo),
                AppUtils.parseZonedDate("dd-MM-yyyy", userInfo.getSections().getWorkingInformation().getGroupOfDOJ(),
                        zoneId),
                userInfo.getSections().getWorkingInformation().getRoleOfIntake(),
                userInfo.getSections().getWorkingInformation().getSwipeMethod(),
                userInfo.getSubOrganization().get(0).getRoleDetails(),
                userInfo.getSections().getHrInformation().getAttendanceFormat(),
                userInfo.getSections().getBasicDetails().getBloodGroup(),
                userInfo.getOrganization(),
                userInfo.getSubOrganization(),
                userInfo.getSections().getBasicDetails().getMaritalStatus(),
                (userInfo.getSections().getHrInformation().getNoticePeriod() != null)
                        ? userInfo.getSections().getHrInformation().getNoticePeriod()
                        : 0,
                userInfo.getSections().getWorkingInformation().getPayrollStatusName(), userInfo.getIdCardDetails());
    }

    private String getEmpName(UserInfo userInfo) {
        String firstName = userInfo.getSections().getBasicDetails().getFirstName();
        String lastName = userInfo.getSections().getBasicDetails().getLastName();
        return firstName + " " + lastName;
    }

    private String getGender(UserInfo userInfo) {
        return userInfo.getSections().getBasicDetails().getGender();
    }

    private String getReviewerName(UserInfo userInfo) {
        if (userInfo.getSections().getHrInformation().getReviewer() != null) {
            String reviewerId = userInfo.getSections().getHrInformation().getReviewer().getManagerId();
            Optional<UserInfo> reviewerInfo = userInfoRepository.findByEmpId(reviewerId);

            if (reviewerInfo.isPresent()) {
                String firstName = reviewerInfo.get().getSections().getBasicDetails().getFirstName();
                String lastNmae = reviewerInfo.get().getSections().getBasicDetails().getLastName();

                return firstName + " " + lastNmae + " - " + reviewerId;
            }
            return reviewerId;
        }
        return "";
    }

    private String getReportingManagerName(UserInfo userInfo) {
        if (userInfo.getSections().getHrInformation().getPrimary() != null) {
            String reportingManagerId = userInfo.getSections().getHrInformation().getPrimary().getManagerId();
            Optional<UserInfo> reportingManagerInfo = userInfoRepository.findByEmpId(reportingManagerId);

            if (reportingManagerInfo.isPresent()) {
                String firstName = reportingManagerInfo.get().getSections().getBasicDetails().getFirstName();
                String lastName = reportingManagerInfo.get().getSections().getBasicDetails().getLastName();
                return firstName + " " + lastName + " - " + reportingManagerId;
            }
            return reportingManagerId;
        }
        return "";
    }

    public String findMaxDevActionStatus() {
        List<UserInfo> users = mongoTemplate.find(Query.query(Criteria.where("tempId").regex("^TEMP\\d+$")),
                UserInfo.class);
        int maxTempNumber = 0;

        for (UserInfo user : users) {
            String tempId = user.getTempId();
            int tempNumber;
            try {
                tempNumber = Integer.parseInt(tempId.substring(4)); // Assuming "tempX" format
            } catch (NumberFormatException e) {
                continue;
            }

            if (tempNumber > maxTempNumber) {
                maxTempNumber = tempNumber;
            }
        }

        return "TEMP" + (maxTempNumber + 1);
    }

    private String generateTempId() {
        String tempId = "";
        boolean isUnique = false;
        while (!isUnique) {
            tempId = String.format("TEMP%03d", tempIdCounter);
            if (!userInfoRepository.existsByTempId(tempId)) {
                isUnique = true;
            } else {
                tempIdCounter++;
            }
        }
        tempIdCounter++;
        return tempId;
    }

    @Override
    public List<EmployeeDetailsDTO> findEmployeesByFilters(String employeeId, String department, String designation,
            String gender, String reviewerId, String reportingManagerId,
            String payRollStatus, String band, String status,
            String organization, String subOrganization, String image) {
        Sort.Direction sort = Sort.Direction.DESC;
        String org = jwtHelper.getOrganizationCode();
        List<UserInfo> employees = userInfoRepository.findAll();

        if (employeeId == null && department == null && designation == null &&
                gender == null && reviewerId == null && reportingManagerId == null &&
                payRollStatus == null && band == null && status == null &&
                organization == null && subOrganization == null && image == null) {

            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getPayrollStatus().equals(org))
                    .collect(Collectors.toList());

            if (sort == Sort.Direction.DESC) {
                Collections.reverse(employees);
            }

            return employees.stream()
                    .map(this::mapToEmployeeDetailsDTO)
                    .collect(Collectors.toList());
        }
        if (employeeId != null && !employeeId.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getEmpId().equals(employeeId))
                    .collect(Collectors.toList());
        }
        if (department != null && !department.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getDepartment().equals(department))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        if (designation != null && !designation.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getDesignation().equals(designation))
                    .collect(Collectors.toList());
        }
        if (gender != null && !gender.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getBasicDetails().getGender().equals(gender))
                    .collect(Collectors.toList());
        }
        if (payRollStatus != null && !payRollStatus.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getRoleOfIntake().equals(payRollStatus))
                    .collect(Collectors.toList());
        }
        if (band != null && !band.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getGrade().equals(band))
                    .collect(Collectors.toList());
        }
        if (organization != null && !organization.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getOrganization().getOrganizationCode().equals(organization))
                    .collect(Collectors.toList());
        }
        if (subOrganization != null && !subOrganization.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSubOrganization().stream()
                            .anyMatch(subOrg -> subOrg.getOrganizationCode().equals(subOrganization)))
                    .collect(Collectors.toList());
        }
        if (reviewerId != null && !reviewerId.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getHrInformation().getReviewer() != null &&
                            emp.getSections().getHrInformation().getReviewer().getManagerId().equals(reviewerId))
                    .collect(Collectors.toList());
        }
        if (reportingManagerId != null && !reportingManagerId.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getHrInformation().getPrimary() != null &&
                            emp.getSections().getHrInformation().getPrimary().getManagerId().equals(reportingManagerId))
                    .collect(Collectors.toList());
        }
        if (image != null && !image.isEmpty()) {
            if (image.equalsIgnoreCase("with")) {
                employees = employees.stream()
                        .filter(emp -> emp.getIdCardDetails() != null
                                && emp.getIdCardDetails().getIdPhotoByGraphics() != null &&
                                emp.getIdCardDetails().getIdPhotoByGraphics().getFileName() != null)
                        .collect(Collectors.toList());
            }
            if (image.equalsIgnoreCase("without")) {
                employees = employees.stream()
                        .filter(emp -> emp.getIdCardDetails() == null
                                || emp.getIdCardDetails().getIdPhotoByGraphics() == null ||
                                emp.getIdCardDetails().getIdPhotoByGraphics().getFileName() == null)
                        .collect(Collectors.toList());
            }
        }
        if (sort == Sort.Direction.DESC) {
            Collections.reverse(employees);
        }

        return employees.stream()
                .map(this::mapToEmployeeDetailsDTO)
                .collect(Collectors.toList());

    }

    @Override
    public List<EmployeeOrganisationDTO> getOrganisationRoles(String org) {
        Optional<Organization> organizationOpt = organizationRepository.findByOrganizationDetail(org);

        if (organizationOpt.isEmpty() || organizationOpt.get().getSequence() == null) {
            return List.of();
        }

        return organizationOpt.get().getSequence()
                .stream()
                .map(sequence -> {
                    EmployeeOrganisationDTO dto = new EmployeeOrganisationDTO();
                    dto.setLabel(sequence.getRoleType());
                    dto.setValue(sequence.getRoleType());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<EmployeeOrganisationDTO> getAllOrgansSubOrg() {
        List<Organization> organizations = organizationRepository.findAll();

        if (organizations.isEmpty()) {
            return List.of();
        }
        return organizations.stream()
                .map(org -> {
                    EmployeeOrganisationDTO dto = new EmployeeOrganisationDTO();
                    dto.setLabel(org.getOrganizationDetail());
                    dto.setValue(org.getOrganizationCode());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getAllReviewerDetails() {
        return userInfoRepository.findAll()
                .stream()
                .map(userInfo -> {
                    if (userInfo.getSections() != null && userInfo.getSections().getHrInformation() != null
                            && userInfo.getSections().getHrInformation().getReviewer() != null) {

                        String empId = userInfo.getSections().getHrInformation().getReviewer().getManagerId();
                        Optional<UserInfo> reviewerInfoOptional = userInfoRepository.findByEmpId(empId);

                        if (reviewerInfoOptional.isPresent()) {
                            UserInfo reviewerInfo = reviewerInfoOptional.get();
                            String name = (reviewerInfo.getSections() != null
                                    && reviewerInfo.getSections().getBasicDetails() != null)
                                            ? reviewerInfo.getSections().getBasicDetails().getFirstName() + " "
                                                    + reviewerInfo.getSections().getBasicDetails().getLastName()
                                            : "Unknown";

                            return Map.of(
                                    "name", name + " - " + empId,
                                    "value", empId);
                        }
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .filter(map -> map.get("value") != null && !map.get("value").isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public ProfileDetailsDTO getEmployeeBasicDetails(String empId) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);

        if (userInfoOptional.isEmpty()) {
            return null;
        }

        UserInfo userInfo = userInfoOptional.get();
        Sections sections = userInfo.getSections();

        ProfileDetailsDTO profileDetailsDTO = new ProfileDetailsDTO();
        profileDetailsDTO.setProfileOverallSubmit(sections.getProfileOverallSubmit());

        if (sections != null && sections.getBasicDetails() != null) {
            BasicDetails basicDetails = sections.getBasicDetails();
            profileDetailsDTO.setEmployeeName(basicDetails.getFirstName() + " " +
                    (basicDetails.getLastName() != null ? basicDetails.getLastName() : ""));
            if (basicDetails.getDob() != null) {
                ZonedDateTime dobZoned = basicDetails.getDob()
                        .toInstant()
                        .atZone(ZoneId.systemDefault());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                profileDetailsDTO.setDob(dobZoned.format(formatter));
            }

        }

        if (sections != null && sections.getContact() != null) {
            Contact contact = sections.getContact();
            profileDetailsDTO.setPersonalEmailId(contact.getPersonalEmailId());
            profileDetailsDTO.setContactNumber(contact.getPrimaryContactNumber());
        }

        if (sections != null && sections.getWorkingInformation() != null) {
            profileDetailsDTO.setWorkLocation(sections.getWorkingInformation().getWorkLocation());
            profileDetailsDTO.setDesignation(sections.getWorkingInformation().getDesignation());
        }

        if (sections != null && sections.getProfilePicture() != null) {
            FilePathStruct profilePicture = sections.getProfilePicture();
            profileDetailsDTO
                    .setProfileImage(new ImageDTO(profilePicture.getFolderName(), profilePicture.getFileName()));
        }

        if (sections != null && sections.getBannerImage() != null) {
            FilePathStruct bannerImage = sections.getBannerImage();
            profileDetailsDTO.setBannerImage(new ImageDTO(bannerImage.getFolderName(), bannerImage.getFileName()));
        }

        return profileDetailsDTO;
    }

    @Override
    public String updateProfileOverallSubmit(String profileOverallSubmit) {
        String authenticatedEmpId = jwtHelper.getUserRefDetail().getEmpId();
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(authenticatedEmpId);

        if (userInfoOptional.isPresent()) {
            UserInfo user = userInfoOptional.get();
            String existingSubmitStatus = user.getSections().getProfileOverallSubmit();
            if ("Save".equals(profileOverallSubmit) &&
                    ("pending".equals(existingSubmitStatus) || "Save".equals(existingSubmitStatus))) {
                user.getSections().setProfileOverallSubmit(profileOverallSubmit);
                userInfoRepository.save(user);
                return "Profile saved successfully.";
            }

            if ("Submit".equals(profileOverallSubmit) &&
                    ("pending".equals(existingSubmitStatus) || "Save".equals(existingSubmitStatus))) {
                user.getSections().setProfileOverallSubmit(profileOverallSubmit);
                userInfoRepository.save(user);
                return "Profile submitted successfully.";
            }

            if ("Submit".equals(existingSubmitStatus)) {
                return "Profile Already Submitted";
            }

            return "Invalid update. Only 'Save' is allowed multiple times, and 'Submit' can only be done once.";
        } else {
            return "Employee not found.";
        }
    }

    @Override
    public List<Map<String, String>> getRoleOfIntake(String organizationCode) {
        Organization organization = organizationRepository.findByOrganizationCode(organizationCode).orElse(null);

        if (organization == null) {
            return Collections.emptyList();
        }

        return organization.getSequence().stream()
                .map(sequence -> {
                    Map<String, String> roleMap = new HashMap<>();
                    roleMap.put("value", sequence.getRoleType());
                    roleMap.put("name", sequence.getRoleType());
                    return roleMap;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getEmployeesByPrimaryManager(String primaryManagerId) {
        return userInfoRepository.findAll()
                .stream()
                .filter(userInfo -> userInfo.getSections() != null
                        && userInfo.getSections().getHrInformation() != null
                        && userInfo.getSections().getHrInformation().getPrimary() != null
                        && primaryManagerId
                                .equals(userInfo.getSections().getHrInformation().getPrimary().getManagerId()))
                .map(userInfo -> {
                    String empId = userInfo.getEmpId();
                    String name = (userInfo.getSections() != null && userInfo.getSections().getBasicDetails() != null)
                            ? userInfo.getSections().getBasicDetails().getFirstName() + " " +
                                    userInfo.getSections().getBasicDetails().getLastName()
                            : "Unknown";

                    return Map.of(
                            "name", name + " - " + empId,
                            "value", empId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public byte[] sampleExcelDownload() throws IOException {
        List<HeaderList> headers = excelExport.getOrDefault(ExcelType.ADD_EMPLOYEE.label, null).prepareHeaders();
        List<ExcelBuilder.DropdownConfig> dropdowns = excelExport.getOrDefault(ExcelType.ADD_EMPLOYEE.label, null)
                .prepareDropdowns();
        log.info("Excel Headers: {}", headers);
        ExcelBuilder excelBuilder = new ExcelBuilder.Builder()
                .setHeaders(headers)
                .setDropdowns(dropdowns)
                .build();

        return excelBuilder.buildExcel();

    }

    @Override
    public byte[] excelImport(MultipartFile file) throws IOException, InterruptedException, ExecutionException {
        return new byte[0];
    }

    @Override
    public Page<EmployeeDetailsDTO> findEmployeesBypage(
            String employeeId, String department, String designation, String gender,
            String reviewerId, String reportingManagerId, String payRollStatus, String band,
            String status, String organization, String subOrganization, Pageable pageReq, String query) {

        List<UserInfo> employees;

        boolean isFilterApplied = employeeId != null || department != null || designation != null ||
                gender != null || reviewerId != null || reportingManagerId != null ||
                payRollStatus != null || band != null || status != null ||
                organization != null || subOrganization != null;

        if (!isFilterApplied) {
            employees = userInfoRepository.findAll();
        } else {
            employees = new ArrayList<>(userInfoRepository.findAll(pageReq).getContent());
        }

        if (employeeId != null && !employeeId.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getEmpId().equals(employeeId))
                    .collect(Collectors.toList());
        }
        if (department != null && !department.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getDepartment().equals(department))
                    .collect(Collectors.toList());
        }
        if (status != null && !status.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getStatus().equals(status))
                    .collect(Collectors.toList());
        }
        if (designation != null && !designation.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getDesignation().equals(designation))
                    .collect(Collectors.toList());
        }
        if (gender != null && !gender.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getBasicDetails().getGender().equals(gender))
                    .collect(Collectors.toList());
        }
        if (payRollStatus != null && !payRollStatus.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getRoleOfIntake().equals(payRollStatus))
                    .collect(Collectors.toList());
        }
        if (band != null && !band.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getWorkingInformation().getGrade().equals(band))
                    .collect(Collectors.toList());
        }
        if (organization != null && !organization.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getOrganization().equals(organization))
                    .collect(Collectors.toList());
        }
        if (subOrganization != null && !subOrganization.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSubOrganization().equals(subOrganization))
                    .collect(Collectors.toList());
        }
        if (reviewerId != null && !reviewerId.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getHrInformation().getReviewer() != null &&
                            emp.getSections().getHrInformation().getReviewer().getManagerId().equals(reviewerId))
                    .collect(Collectors.toList());
        }
        if (reportingManagerId != null && !reportingManagerId.isEmpty()) {
            employees = employees.stream()
                    .filter(emp -> emp.getSections().getHrInformation().getPrimary() != null &&
                            emp.getSections().getHrInformation().getPrimary().getManagerId().equals(reportingManagerId))
                    .collect(Collectors.toList());
        }

        // Convert to DTO
        List<EmployeeDetailsDTO> employeeDTOs = employees.stream()
                .map(this::mapToEmployeeDetailsDTO)
                .collect(Collectors.toList());

        // Convert to paginated response
        int start = (int) pageReq.getOffset();
        int end = Math.min((start + pageReq.getPageSize()), employeeDTOs.size());
        List<EmployeeDetailsDTO> paginatedList = employeeDTOs.subList(start, end);

        return new PageImpl<>(paginatedList, pageReq, employeeDTOs.size());
    }

    @Override
    public List<UserInfo> findByAll(String all) {
        List<UserInfo> string = userInfoRepository.findAll(Sort.by(Sort.Direction.ASC, all));
        Optional<UserInfo> aLong = string.stream().findFirst();
        // System.out.println(aLong);
        return userInfoRepository.findAll(Sort.by(Sort.Direction.ASC, all));
    }

    @Override
    public GenericResponse<String> updateIdCardByHr(String empId, String empRelation, String nameOfRelation,
            String contactNo, String status) throws IOException {

        String updateBy = jwtHelper.getUserRefDetail().getEmpId();
        UserInfo user = userInfoRepository.findByEmpId(empId).orElse(null);

        IdCard idCardEntity = new IdCard();
        idCardEntity.setEmergencyRelationship(empRelation);
        idCardEntity.setRelationshipName(nameOfRelation);
        idCardEntity.setEmergencyContactNo(contactNo);
        idCardEntity.setFileName("HEPL00009.9baa1d6a968bd8de45cf7b6c12efd741.pdf");
        idCardEntity.setFolderName("ID_CARD_BY_HR");
        idCardEntity.setIdCardStatusByHr(status);
        idCardEntity.setCreatedBy(updateBy);
        idCardEntity.setSubmittedOn(LocalDate.now().toString());

        IdCardDetails idCardDetails = user.getIdCardDetails();
        if (idCardDetails == null) {
            idCardDetails = new IdCardDetails();
        }

        idCardDetails.setIdCardByHr(idCardEntity);
        user.setIdCardDetails(idCardDetails);

        userInfoRepository.save(user);

        return GenericResponse.success("Id card updated successfully");
    }

    @Override
    public String updatePassword(String empId, PasswordDTO password) {
        String empIds = jwtHelper.getUserRefDetail().getEmpId();
        UserInfo userInfo = userInfoRepository.findByEmpId(empIds).orElse(null);

        if (userInfo == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.USER_NOT_FOUND);
        }
        if (password.getNewPassword() == null || password.getNewPassword().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PASSWORD_NOT_EMPTY);
        }
        if (password.getNewPassword().length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PASSWORD_MUST_BE);

        }
        if (!password.getNewPassword().equals(password.getConfirmPassword())) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PASSWORD_NOT_MATCH);
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(password.getNewPassword());

        userInfo.setPassword(encodedPassword);
        userInfoRepository.save(userInfo);
        return translator.toLocale(AppMessages.PASSWORD_UPDATED_SUCCESSFULLY);
    }

}