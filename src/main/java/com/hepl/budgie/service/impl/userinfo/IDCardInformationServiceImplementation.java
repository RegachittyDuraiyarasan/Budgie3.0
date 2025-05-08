package com.hepl.budgie.service.impl.userinfo;

import com.google.zxing.WriterException;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.idCard.IdCardGenerationDto;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.userinfo.EmergencyContacts;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.PdfService;
import com.hepl.budgie.service.TemplateService;
import com.hepl.budgie.service.userinfo.IDCardInformationService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class IDCardInformationServiceImplementation implements IDCardInformationService {

    private final UserInfoRepository userInfoRepository;
    private final FileService fileService;
    private final PdfService pdfService;
    private final TemplateService templateService;

    @Override
    public IdCardGenerationDto iDCardInformation(String empId) {
//        String empId = user.getEmpId();
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);
        if (userInfoOptional.isEmpty()) {
            log.error("UserInfo not found for empId: {}", empId);
            return null;
        }
        UserInfo userInfo = userInfoOptional.get();
        IdCardGenerationDto idCard = new IdCardGenerationDto();
        idCard.setFirstName(userInfo.getSections().getBasicDetails().getFirstName());
        idCard.setMiddleName(userInfo.getSections().getBasicDetails().getMiddleName());
        idCard.setLastName(userInfo.getSections().getBasicDetails().getLastName());
        idCard.setBloodGroup(userInfo.getSections().getBasicDetails().getBloodGroup());
        idCard.setWorkLocation(userInfo.getSections().getWorkingInformation().getWorkLocation());
        idCard.setPrimaryContactNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
        idCard.setSecondaryContactNumber(userInfo.getSections().getContact().getSecondaryContactNumber());

//        EmergencyContacts emergencyContact = userInfo.getSections().getFamily().getEmergencyContacts().stream()
//                .filter(EmergencyContacts::isEmergencyContact)
//                .findFirst()
//                .orElse(null);
//
//        if (emergencyContact != null) {
//            idCard.setEmergencyContactOfRelationship(emergencyContact.getRelationship());
//            idCard.setNameOfRelationship(emergencyContact.getContactName());
//            idCard.setEmergencyContactNo(emergencyContact.getContactNumber());
//        }

        idCard.setDateOfJoining(convertToLocalDate(userInfo.getSections().getWorkingInformation().getDoj()));
        idCard.setEmployeeCode(userInfo.getEmpId());
        idCard.setOfficialEmailID(userInfo.getSections().getWorkingInformation().getOfficialEmail());
        idCard.setPersonalEmailID(userInfo.getSections().getContact().getPersonalEmailId());
        idCard.setGroupDOJ(convertToLocalDate(userInfo.getSections().getWorkingInformation().getGroupOfDOJ()));
        idCard.setPreferredDateOfBirth(convertToLocalDate(userInfo.getSections().getBasicDetails().getDob()));
        if(userInfo.getIdCardDetails() != null) {
            if(userInfo.getIdCardDetails().getIdCardByHr() != null) {
                idCard.setEmergencyContactOfRelationship(userInfo.getIdCardDetails().getIdCardByHr().getEmergencyRelationship() != null ? userInfo.getIdCardDetails().getIdCardByHr().getEmergencyRelationship() : null);
                idCard.setNameOfRelationship(userInfo.getIdCardDetails().getIdCardByHr().getRelationshipName() != null ? userInfo.getIdCardDetails().getIdCardByHr().getRelationshipName() : null);
                idCard.setEmergencyContactNo(userInfo.getIdCardDetails().getIdCardByHr().getEmergencyContactNo() != null ? userInfo.getIdCardDetails().getIdCardByHr().getEmergencyContactNo() : null);
                idCard.setStatus(userInfo.getIdCardDetails().getIdCardByHr().getIdCardStatusByHr() != null ? userInfo.getIdCardDetails().getIdCardByHr().getIdCardStatusByHr() : null);
            }
        }
        if(userInfo.getIdCardDetails() != null){
            if(userInfo.getIdCardDetails().getIdPhotoByGraphics() != null){
                idCard.setGraphicsImage(userInfo.getIdCardDetails().getIdPhotoByGraphics().getFileName() != null ? userInfo.getIdCardDetails().getIdPhotoByGraphics().getFileName() : null);
                idCard.setGraphicsImageFolderName(userInfo.getIdCardDetails().getIdPhotoByGraphics().getFolderName()!= null ? userInfo.getIdCardDetails().getIdPhotoByGraphics().getFolderName() : null);
            }
        }
        return idCard;
    }

    @Override
    public IdCardGenerationDto updateIdCard(FormRequest form, UserInfo user) throws IOException, WriterException {
        String empId = user.getEmpId();
        log.info("empId {}", empId);
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);
        if (userInfoOptional.isEmpty()) {
            log.error("UserInfo not found for empId: {}", empId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();
        Map<String, Object> formFields = form.getFormFields();

        if (formFields.containsKey("firstName") && formFields.get("firstName") != null) {
            String firstName = formFields.get("firstName").toString();
            userInfo.getSections().getBasicDetails().setFirstName(firstName);
        }

        if (formFields.containsKey("lastName") && formFields.get("lastName") != null) {
            String lastName = formFields.get("lastName").toString();
            userInfo.getSections().getBasicDetails().setLastName(lastName);
        }
        if (formFields.containsKey("bloodGroup") && formFields.get("bloodGroup") != null) {
            userInfo.getSections().getBasicDetails().setBloodGroup(formFields.get("bloodGroup").toString());
        }
        if (formFields.containsKey("middleName") && formFields.get("middleName") != null) {
            userInfo.getSections().getBasicDetails().setMiddleName(formFields.get("middleName").toString());
        }
        if (formFields.containsKey("workLocation") && formFields.get("workLocation") != null) {
            userInfo.getSections().getWorkingInformation().setWorkLocation(formFields.get("workLocation").toString());
        }
        if (formFields.containsKey("primaryContactNumber") && formFields.get("primaryContactNumber") != null) {
            userInfo.getSections().getContact()
                    .setPrimaryContactNumber(formFields.get("primaryContactNumber").toString());
        }
        if (formFields.containsKey("secondaryContactNumber") && formFields.get("secondaryContactNumber") != null) {
            userInfo.getSections().getContact()
                    .setSecondaryContactNumber(formFields.get("secondaryContactNumber").toString());
        }
        if (formFields.containsKey("emergencyContactOfRelationship")
                && formFields.get("emergencyContactOfRelationship") != null) {
            EmergencyContacts emergencyContact = userInfo.getSections().getFamily().getEmergencyContacts().stream()
                    .filter(EmergencyContacts::isEmergencyContact)
                    .findFirst()
                    .orElse(null);
            if (emergencyContact != null) {
                emergencyContact.setRelationship(formFields.get("emergencyContactOfRelationship").toString());
            }
        }
        if (formFields.containsKey("nameOfRelationship") && formFields.get("nameOfRelationship") != null) {
            EmergencyContacts emergencyContact = userInfo.getSections().getFamily().getEmergencyContacts().stream()
                    .filter(EmergencyContacts::isEmergencyContact)
                    .findFirst()
                    .orElse(null);
            if (emergencyContact != null) {
                emergencyContact.setContactName(formFields.get("nameOfRelationship").toString());
            }
        }
        if (formFields.containsKey("emergencyContactNo") && formFields.get("emergencyContactNo") != null) {
            EmergencyContacts emergencyContact = userInfo.getSections().getFamily().getEmergencyContacts().stream()
                    .filter(EmergencyContacts::isEmergencyContact)
                    .findFirst()
                    .orElse(null);
            if (emergencyContact != null) {
                emergencyContact.setContactNumber(formFields.get("emergencyContactNo").toString());
            }
        }

        if (formFields.containsKey("dateOfJoining") && formFields.get("dateOfJoining") != null) {
            userInfo.getSections().getWorkingInformation()
                    .setDoj(convertToZonedDateTime(formFields.get("dateOfJoining").toString()));
        }
        if (formFields.containsKey("employeeCode") && formFields.get("employeeCode") != null) {
            String employeeCode = formFields.get("employeeCode").toString();
            if (!employeeCode.equals(empId)) {
                log.error("Mismatch between empId and employeeCode. empId: {}, employeeCode: {}", empId, employeeCode);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Employee code must match the existing employee ID.");
            }
        }
        if (formFields.containsKey("officialEmailID") && formFields.get("officialEmailID") != null) {
            userInfo.getSections().getWorkingInformation()
                    .setOfficialEmail(formFields.get("officialEmailID").toString());
        }
        if (formFields.containsKey("personalEmailID") && formFields.get("personalEmailID") != null) {
            userInfo.getSections().getContact().setPersonalEmailId(formFields.get("personalEmailID").toString());
        }
        if (formFields.containsKey("groupDOJ") && formFields.get("groupDOJ") != null) {
            userInfo.getSections().getWorkingInformation()
                    .setGroupOfDOJ(convertToZonedDateTime(formFields.get("groupDOJ").toString()));
        }
        if (formFields.containsKey("preferredDateOfBirth") && formFields.get("preferredDateOfBirth") != null) {
            userInfo.getSections().getBasicDetails()
                    .setPreferredDob(convertToZonedDateTime(formFields.get("preferredDateOfBirth").toString()));
        }

        String firstName = formFields.containsKey("firstName") ? formFields.get("firstName").toString() : "";
        String lastName = formFields.containsKey("lastName") ? formFields.get("lastName").toString() : "";
        String empId1 = formFields.containsKey("empId") ? formFields.get("empId").toString() : "";
        String secondaryContactNumber = formFields.containsKey("secondaryContactNumber")
                ? formFields.get("secondaryContactNumber").toString()
                : "";
        String officialEmailID = formFields.containsKey("officialEmailID")
                ? formFields.get("officialEmailID").toString()
                : "";
        String dateOfJoining = formFields.containsKey("dateOfJoining") ? formFields.get("dateOfJoining").toString()
                : "";
        String bloodGroup = formFields.containsKey("bloodGroup") ? formFields.get("bloodGroup").toString() : "";
        MultipartFile candidatePhoto = (MultipartFile) formFields.get("file");
        String dojFormatted = userInfo.getSections().getWorkingInformation().getDoj().toLocalDate().toString();
        String rqDate = "Emp ID :" + empId +
                ", UserName : " + userInfo.getSections().getBasicDetails().getFirstName() + " "
                + userInfo.getSections().getBasicDetails().getLastName().toUpperCase() + "\n" +
                ", Date Of joining : " + dojFormatted + "\n" +
                ", Emergency Contact :" + userInfo.getSections().getContact().getSecondaryContactNumber() + "\n" +
                ", Blood Group : " + userInfo.getSections().getBasicDetails().getBloodGroup() + "\n" +
                ", Official Email : " + userInfo.getSections().getWorkingInformation().getOfficialEmail();
        byte[] qrCodeBytes = AppUtils.generateQRCode(rqDate, 300, 300);
        byte[] idCardBytes = pdfService.generatePdf(
                templateService.getIdCardInfo(Base64.getEncoder().encodeToString(candidatePhoto.getBytes()), firstName,
                        lastName, secondaryContactNumber, empId1, officialEmailID, dateOfJoining, bloodGroup,
                        Base64.getEncoder().encodeToString(qrCodeBytes)));
        String idCardFileName = "idCard_" + empId + ".pdf";
//        userInfo.getIdCardDetails().setFileName(idCardFileName);
        fileService.uploadFile(idCardBytes, FileType.ID_CARD, idCardFileName);
        userInfoRepository.save(userInfo);
        return mapToIdCardDto(userInfo);
    }

    private ZonedDateTime convertToZonedDateTime(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(dateString, formatter);
        return ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.systemDefault());
    }

    private IdCardGenerationDto mapToIdCardDto(UserInfo userInfo) {
        IdCardGenerationDto idCard = new IdCardGenerationDto();
        idCard.setFirstName(userInfo.getSections().getBasicDetails().getFirstName());
        idCard.setMiddleName(userInfo.getSections().getBasicDetails().getMiddleName());
        idCard.setLastName(userInfo.getSections().getBasicDetails().getLastName());
        idCard.setBloodGroup(userInfo.getSections().getBasicDetails().getBloodGroup());
        if (userInfo.getSections().getBasicDetails().getPreferredDob() != null) {
            idCard.setPreferredDateOfBirth(userInfo.getSections().getBasicDetails().getPreferredDob().toLocalDate());
        } else {
            idCard.setPreferredDateOfBirth(null);
        }

        idCard.setWorkLocation(userInfo.getSections().getWorkingInformation().getWorkLocation());
        idCard.setDateOfJoining(userInfo.getSections().getWorkingInformation().getDoj().toLocalDate());
        idCard.setEmployeeCode(userInfo.getEmpId());
        idCard.setOfficialEmailID(userInfo.getSections().getWorkingInformation().getOfficialEmail());
        idCard.setGroupDOJ(userInfo.getSections().getWorkingInformation().getGroupOfDOJ().toLocalDate());

        idCard.setPrimaryContactNumber(userInfo.getSections().getContact().getPrimaryContactNumber());
        idCard.setSecondaryContactNumber(userInfo.getSections().getContact().getSecondaryContactNumber());
        idCard.setPersonalEmailID(userInfo.getSections().getContact().getPersonalEmailId());

        EmergencyContacts emergencyContact = userInfo.getSections().getFamily().getEmergencyContacts().stream()
                .filter(EmergencyContacts::isEmergencyContact)
                .findFirst()
                .orElse(null);

        if (emergencyContact != null) {
            idCard.setEmergencyContactOfRelationship(emergencyContact.getRelationship());
            idCard.setNameOfRelationship(emergencyContact.getContactName());
            idCard.setEmergencyContactNo(emergencyContact.getContactNumber());
        }

        return idCard;
    }

    private LocalDate convertToLocalDate(ZonedDateTime zonedDateTime) {
        if (zonedDateTime != null) {
            return zonedDateTime.toLocalDate();
        }
        return null;
    }
}
