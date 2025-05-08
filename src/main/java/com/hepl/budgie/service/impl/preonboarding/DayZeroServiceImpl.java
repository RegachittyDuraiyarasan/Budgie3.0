package com.hepl.budgie.service.impl.preonboarding;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.preonboarding.DocumentDetailDTO;
import com.hepl.budgie.dto.preonboarding.TodayJoiningDTO;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.Sequence;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import com.hepl.budgie.entity.userinfo.*;
import com.hepl.budgie.repository.preOnboardingRepository.OnboardingInfoRepository;
import com.hepl.budgie.repository.userinfo.OtherDocumentsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.utils.AppMessages;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.hepl.budgie.entity.preonboarding.DayZeroResponse;
import com.hepl.budgie.service.preonboarding.DayZeroService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DayZeroServiceImpl implements DayZeroService {

    private final MongoTemplate mongoTemplate;
    private final OtherDocumentsRepository otherDocumentsRepository;
    private final OnboardingInfoRepository onboardingInfoRepository;
    private final UserInfoRepository userInfoRepository;


    @Override
    public List<DayZeroResponse> fetchDayZeroData() {
        ZonedDateTime tomorrowStart = ZonedDateTime.now().plusDays(1).withHour(0).withMinute(0).withSecond(0)
                .withNano(0);
        ZonedDateTime tomorrowEnd = tomorrowStart.plusDays(1).minusNanos(1);
        Query query = new Query();
        query.addCriteria(Criteria.where("sections.workingInformation.doj").gte(tomorrowStart).lte(tomorrowEnd));

        List<UserInfo> userInfos = mongoTemplate.find(query, UserInfo.class);

        List<DayZeroResponse> dayZeroResponses = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            DayZeroResponse response = new DayZeroResponse();
            String firstName = userInfo.getSections().getBasicDetails().getFirstName();
            String formattedFirstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);

            response.setEmpId(formattedFirstName + " - " + userInfo.getEmpId());
            response.setPersonalMailId(userInfo.getSections().getContact().getPersonalEmailId());
            response.setContactNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
            response.setInductionMailStatus(false);
            response.setBuddyMailStatus(false);

            dayZeroResponses.add(response);
        }
        return dayZeroResponses;
    }

    @Override
    public List<TodayJoiningDTO> fetchTodayDateOfJoining() {
        ZonedDateTime todayStart = ZonedDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime todayEnd = todayStart.plusDays(1).minusNanos(1);

        Query query = new Query();
        query.addCriteria(Criteria.where("sections.workingInformation.doj").gte(todayStart).lte(todayEnd));

        List<UserInfo> userInfos = mongoTemplate.find(query, UserInfo.class);
        List<TodayJoiningDTO> todayJoiningList = new ArrayList<>();

        for (UserInfo userInfo : userInfos) {
            TodayJoiningDTO todayJoiningDTO = new TodayJoiningDTO();

            String empId = userInfo.getEmpId();
            Optional<OnBoardingInfo> onBoardingInfoOptional = onboardingInfoRepository.findByEmpId(empId);
            if (onBoardingInfoOptional.isPresent()) {
                OnBoardingInfo onBoardingInfo = onBoardingInfoOptional.get();
                todayJoiningDTO.setOnboardingStatus(onBoardingInfo.isOnboardingStatus());
            } else {
                todayJoiningDTO.setOnboardingStatus(false);
            }

            todayJoiningDTO.setEmpId(userInfo.getSections().getBasicDetails().getFirstName() + " - " + empId);
            todayJoiningDTO.setMobileNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
            todayJoiningDTO.setEmail(userInfo.getSections().getContact().getPersonalEmailId());
            todayJoiningDTO.setEmpIdGeneratedStatus(userInfo.isEmpIdGenerateStatus());
            todayJoiningDTO.setDocumentVerification(false);
            todayJoiningDTO.setInductionMail(false);
            todayJoiningDTO.setBuddyMail(false);
            todayJoiningDTO.setAction(false);
            todayJoiningDTO.setDocumentStatus(false);
            todayJoiningDTO.setTempId(empId);

            Optional<UserOtherDocuments> optionalUserDocs = otherDocumentsRepository.findByEmpId(empId);
            if (optionalUserDocs.isPresent()) {
                UserOtherDocuments userOtherDocuments = optionalUserDocs.get();
                if ("Approved".equalsIgnoreCase(userOtherDocuments.getStatus())) {
                    todayJoiningDTO.setDocumentVerification(true);
                    todayJoiningDTO.setDocumentStatus(true);
                }
            }

            Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);
            if (userInfoOptional.isPresent()) {
                UserInfo userInfoData = userInfoOptional.get();
                if (userInfoData.isEmpIdGenerateStatus()) {
                    todayJoiningDTO.setAction(true);
                }
            }

            // Query for UserOtherDocuments
            Query documentQuery = new Query();
            documentQuery.addCriteria(Criteria.where("empId").is(empId));

            UserOtherDocuments userDocument = mongoTemplate.findOne(documentQuery, UserOtherDocuments.class);
            if (userDocument != null) {
                todayJoiningDTO.setDocumentStatus("Approved".equalsIgnoreCase(userDocument.getStatus()));
            }

            // Fetch Organization based on roleOfIntake
            String roleOfIntake = userInfo.getSections().getWorkingInformation().getRoleOfIntake();
            Query orgQuery = new Query();
            orgQuery.addCriteria(Criteria.where("sequence.roleType").is(roleOfIntake));

            Organization organization = mongoTemplate.findOne(orgQuery, Organization.class);
            if (organization != null) {
                // Look for the matching sequence
                for (Sequence sequence : organization.getSequence()) {
                    if (sequence.getRoleType().equals(roleOfIntake)) {
                        todayJoiningDTO.setGenerationStatus("Yes".equalsIgnoreCase(sequence.getAutoGenerationStatus()));
                        break;
                    }
                }
            }

            todayJoiningList.add(todayJoiningDTO);
        }

        return todayJoiningList;
    }

    @Override
    public List<DocumentDetailDTO> getDocuments(String empId) {
        Optional<UserOtherDocuments> userInfo = otherDocumentsRepository.findByEmpId(empId);
        List<DocumentDetailDTO> documents = new ArrayList<>();

        if (userInfo.isPresent()) {
            Documents userDocuments = userInfo.get().getDocuments();
            if (userDocuments != null) {
                safelyUpdateDocumentStatus(documents, "passportPhoto", userDocuments.getPassportPhoto());
                safelyUpdateDocumentStatus(documents, "resume", userDocuments.getResume());
                safelyUpdateDocumentStatus(documents, "payslips", userDocuments.getPayslips());
                safelyUpdateDocumentStatus(documents, "pan", userDocuments.getPan());
                safelyUpdateDocumentStatus(documents, "vaccination", userDocuments.getVaccination());
                safelyUpdateDocumentStatus(documents, "signature", userDocuments.getSignature());
                safelyUpdateDocumentStatus(documents, "dateOfBirthProof", userDocuments.getDateOfBirthProof());
                safelyUpdateDocumentStatus(documents, "bloodGroupProof", userDocuments.getBloodGroupProof());
                safelyUpdateDocumentStatus(documents, "aadhaarCard", userDocuments.getAadhaarCard());
                safelyUpdateDocumentStatus(documents, "bankPassbook", userDocuments.getBankPassbook());
                safelyUpdateDocumentStatus(documents, "relievingLetter", userDocuments.getRelievingLetter());
            }
        }

        return documents;
    }

    private void safelyUpdateDocumentStatus(List<DocumentDetailDTO> documents, String documentName,
            PassportPhoto document) {
        if (document != null && document.getFileName() != null && !document.getFileName().isEmpty()) {
            String status = (document.getStatus() == null || document.getStatus().isEmpty()) ? "pending"
                    : document.getStatus();
            String cleanFileName = document.getFileName().substring(document.getFileName().indexOf('_') + 1);

            documents.add(new DocumentDetailDTO(
                    documentName,
                    Collections.singletonList(document.getFileName()),
                    document.getFolderName(),
                    Collections.singletonList(cleanFileName),
                    status));
        } else {
            documents.add(new DocumentDetailDTO(
                    documentName,
                    Collections.emptyList(),
                    "",
                    Collections.emptyList(),
                    "pending"));
        }
    }

    private void safelyUpdateDocumentStatus(List<DocumentDetailDTO> documents, String documentName,
            List<PassportPhoto> documentList) {
        if (documentList != null && !documentList.isEmpty()) {
            List<String> filePaths = new ArrayList<>();
            List<String> titles = new ArrayList<>();
            String folderName = "";
            String status = "pending";

            for (PassportPhoto document : documentList) {
                if (document != null && document.getFileName() != null && !document.getFileName().isEmpty()) {
                    if (status.equals("pending") && document.getStatus() != null && !document.getStatus().isEmpty()) {
                        status = document.getStatus();
                    }
                    filePaths.add(document.getFileName());
                    titles.add(document.getFileName().substring(document.getFileName().indexOf('_') + 1));
                    folderName = document.getFolderName();
                }
            }

            if (!filePaths.isEmpty()) {
                documents.add(new DocumentDetailDTO(documentName, filePaths, folderName, titles, status));
            } else {
                documents.add(new DocumentDetailDTO(documentName, Collections.emptyList(), "", Collections.emptyList(),
                        "pending"));
            }
        }
    }

    @Override
    public GenericResponse<Map<String, Object>> toggleFile(String empId, Map<String, String> documentStatusesInput,
            String overallStatus) {
        Optional<UserOtherDocuments> userInfoOptional = otherDocumentsRepository.findByEmpId(empId);

        if (userInfoOptional.isEmpty()) {
            return GenericResponse.<Map<String, Object>>builder()
                    .status(false)
                    .message(AppMessages.EMPLOYEE_NOT_FOUND)
                    .errorType("NOT_FOUND")
                    .timestamp(System.currentTimeMillis())
                    .data(Collections.emptyMap())
                    .build();
        }

        UserOtherDocuments userInfo = userInfoOptional.get();
        Documents userDocuments = userInfo.getDocuments();

        if (userDocuments == null) {
            return GenericResponse.<Map<String, Object>>builder()
                    .status(false)
                    .message(AppMessages.DOCUMENT_NOT_FOUND)
                    .errorType("NOT_FOUND")
                    .timestamp(System.currentTimeMillis())
                    .data(Collections.emptyMap())
                    .build();
        }

        boolean updated = false;
        int approvedDocumentCount = 0;
        Map<String, String> documentStatuses = new HashMap<>();
        for (Map.Entry<String, String> entry : documentStatusesInput.entrySet()) {
            String documentName = entry.getKey();
            String status = entry.getValue();

            if ("Approved".equalsIgnoreCase(status)) {
                Object document = getDocumentByName(userDocuments, documentName);

                if (document != null && updateDocumentStatus(document)) {
                    documentStatuses.put(documentName, "Approved");
                    updated = true;
                    approvedDocumentCount++;
                }
            } else {
                documentStatuses.put(documentName, status);
            }
        }

        String finalOverallStatus = "Pending";
        if (approvedDocumentCount >= 3 && "Approved".equalsIgnoreCase(overallStatus)) {
            finalOverallStatus = "Approved";
            userInfo.setStatus(finalOverallStatus);
            otherDocumentsRepository.save(userInfo);
        } else if (approvedDocumentCount < 3) {
            finalOverallStatus = "Constraint Failed";
            return GenericResponse.<Map<String, Object>>builder()
                    .status(false)
                    .message(AppMessages.DOCUMENT_APPROVED)
                    .errorType("CONSTRAINT_FAILED")
                    .timestamp(System.currentTimeMillis())
                    .data(Map.of("result", Map.of("documents_status", documentStatuses, "status", finalOverallStatus)))
                    .build();
        }

        return GenericResponse.<Map<String, Object>>builder()
                .status(true)
                .message(AppMessages.ONBOARDING_INFO)
                .errorType("NONE")
                .timestamp(System.currentTimeMillis())
                .data(Map.of("result", Map.of("documents", documentStatuses, "status", finalOverallStatus)))
                .build();
    }

    @Override
    public UserInfo getByEmpId(String empId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("empId").is(empId));
        UserInfo userInfo = mongoTemplate.findOne(query, UserInfo.class);

        if (userInfo != null && userInfo.getSections() != null && userInfo.getSections().getHrInformation() != null) {
            Optional<OnBoardingInfo> existingOnBoardingInfo = onboardingInfoRepository.findByEmpId(empId);
            OnBoardingInfo onBoardingInfo;

            if (existingOnBoardingInfo.isPresent()) {
                onBoardingInfo = existingOnBoardingInfo.get();
                onBoardingInfo.setOnboardingStatus(true);
            } else {
                onBoardingInfo = new OnBoardingInfo();
                onBoardingInfo.setEmpId(empId);
                onBoardingInfo.setOnboardingStatus(true);
            }

            onboardingInfoRepository.save(onBoardingInfo); 
            mongoTemplate.save(userInfo);
        }

        return userInfo;
    }

    @Override
    public List<UserOtherDocuments> updateVerifiedAt(List<String> empIds) {
        List<UserOtherDocuments> updatedDocuments = new ArrayList<>();
        for (String empId : empIds) {
            Optional<UserOtherDocuments> userInfoOptional = otherDocumentsRepository.findByEmpId(empId);
            if (userInfoOptional.isPresent()) {
                UserOtherDocuments other = userInfoOptional.get();
                other.setVerifiedAt(LocalDateTime.now());
                updatedDocuments.add(otherDocumentsRepository.save(other));
            }

        }
        return updatedDocuments;
    }

    private Object getDocumentByName(Documents userDocuments, String documentName) {
        switch (documentName) {
            case "passportPhoto":
                return userDocuments.getPassportPhoto();
            case "resume":
                return userDocuments.getResume();
            case "payslips":
                return userDocuments.getPayslips();
            case "pan":
                return userDocuments.getPan();
            case "vaccination":
                return userDocuments.getVaccination();
            case "signature":
                return userDocuments.getSignature();
            case "dateOfBirthProof":
                return userDocuments.getDateOfBirthProof();
            case "bloodGroupProof":
                return userDocuments.getBloodGroupProof();
            case "aadhaarCard":
                return userDocuments.getAadhaarCard();
            case "bankPassbook":
                return userDocuments.getBankPassbook();
            case "relievingLetter":
                return userDocuments.getRelievingLetter();
            default:
                return null;
        }
    }

    private boolean updateDocumentStatus(Object document) {
        if (document instanceof PassportPhoto) {
            PassportPhoto passportPhoto = (PassportPhoto) document;
            if (passportPhoto.getStatus() == null || passportPhoto.getStatus().isEmpty()) {
                passportPhoto.setStatus("Approved");
                passportPhoto.setAuthorizedBy("HEPL00001");
                passportPhoto.setAuthorizedOn(ZonedDateTime.now());
                return true;
            }
        } else if (document instanceof List<?>) {
            List<?> documentList = (List<?>) document;
            boolean updated = false;
            for (Object doc : documentList) {
                if (doc instanceof PassportPhoto) {
                    PassportPhoto passportPhoto = (PassportPhoto) doc;
                    if (passportPhoto.getStatus() == null || passportPhoto.getStatus().isEmpty()) {
                        passportPhoto.setStatus("Approved");
                        passportPhoto.setAuthorizedBy("HEPL00001");
                        passportPhoto.setAuthorizedOn(ZonedDateTime.now());
                        updated = true;
                    }
                }
            }
            return updated;
        }
        return false;
    }

}
