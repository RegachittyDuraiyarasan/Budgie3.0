package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.userinfo.EducationDTO;
import com.hepl.budgie.dto.userinfo.EducationRequestDTO;
import com.hepl.budgie.dto.userinfo.UpdateEducationDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.userinfo.DocumentDetails;
import com.hepl.budgie.entity.userinfo.EducationDetails;
import com.hepl.budgie.entity.userinfo.UserExpEducation;
import com.hepl.budgie.mapper.userinfo.EducationInfoMapper;
import com.hepl.budgie.repository.userinfo.UserExpEducationRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.userinfo.EducationInfoService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import com.hepl.budgie.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class EducationInfoServiceImplementation implements EducationInfoService {

    private final UserInfoRepository userInfoRepository;

    private final UserExpEducationRepository userExpEducationRepository;

    private final EducationInfoMapper educationInfoMapper;

    private final FileService fileService;

    private final JWTHelper jwtHelper;

    private final Translator translator;

    private static final String EDUCATION_SEQUENCE = "ED000";

    @Override
    public void addEducation(String empId,EducationRequestDTO eduDetailsDTO, List<MultipartFile> files) {
        // Validate if the employee exists
        userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
        log.info("Found UserInfo for empId {}: Employee exists", empId);

        // Fetch or create UserExpEducation
        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseGet(() -> {
                    UserExpEducation newEducation = new UserExpEducation();
                    newEducation.setEmpId(empId);
                    newEducation.setEducationDetails(new ArrayList<>());
                    return newEducation;
                });

        // Convert DTO to Entity List
        List<EducationDetails> addEduList = mapToEducationDetails(empId, eduDetailsDTO);

        // Filter out duplicate education
        List<String> existingEduIds = userExpEducation.getEducationDetails().stream()
                .map(EducationDetails::getEducationId)
                .toList();

        List<EducationDetails> filteredList = addEduList.stream()
                .filter(edu -> !existingEduIds.contains(edu.getEducationId()))
                .toList();
        userExpEducation.getEducationDetails().addAll(filteredList);

        userExpEducationRepository.save(userExpEducation);

    }

    @Override
    public List<EducationDTO> getEducation(String empId) {
        boolean existsInUserInfo = userInfoRepository.existsByEmpId(empId);

        Optional<UserExpEducation> userExpEducationOpt = userExpEducationRepository.findByEmpId(empId);

        if (!existsInUserInfo && userExpEducationOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        if (userExpEducationOpt.isEmpty()) {
            return Collections.emptyList();
        }

        List<EducationDetails> educationDetailsList = userExpEducationOpt.get().getEducationDetails();

        List<EducationDTO> educationDTOList = educationDetailsList.stream()
                .filter(edu -> edu.getStatus() != null && edu.getStatus().equalsIgnoreCase("Active"))
                .map(educationInfoMapper::mapToDTO)
                .toList();

        return educationDTOList;
    }


    @Override
    public void updateEducation(String empId,String educationId, UpdateEducationDTO educationRequest) {

        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        if (userExpEducation.getEducationDetails() == null || userExpEducation.getEducationDetails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EDUCATION_NOT_FOUND);
        }

        // Find the specific education record to update
        EducationDetails existingEduDetail = userExpEducation.getEducationDetails().stream()
                .filter(edu -> educationId.equals(edu.getEducationId()) && "Active".equalsIgnoreCase(edu.getStatus()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EDUCATION_ID_NOT_FOUND));

        log.info("Found existing education details: {}", existingEduDetail);

        // Validate request data
        if (educationRequest == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid education data");
        }

        log.info("Update details received: {}", educationRequest);

        // Update the existing education details
        existingEduDetail.setQualification(educationRequest.getQualification());
        existingEduDetail.setCourse(educationRequest.getCourse());
        existingEduDetail.setBeginOn(DateUtil.parseDate(educationRequest.getBeginOn()));
        existingEduDetail.setEndOn(DateUtil.parseDate(educationRequest.getEndOn()));
        existingEduDetail.setInstitute(educationRequest.getInstitute());
        existingEduDetail.setPercentageOrCgpa(educationRequest.getPercentageOrCgpa());
        educationRequest.setStatus(existingEduDetail.getStatus());

        // Handle file upload
        try {
            existingEduDetail.setDocumentDetails(
                    (educationRequest.getFiles() != null && !educationRequest.getFiles().isEmpty())
                            ? uploadFileAndGetDocumentDetails(educationRequest.getFiles(), empId) // New file Upload
                            : existingEduDetail.getDocumentDetails() // No new file Retain existing or null
            );
        } catch (IOException e) {
            log.error("Failed to upload file for education details", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FILE_UPLOAD);
        }


        userExpEducationRepository.save(userExpEducation);
        log.info("Successfully updated education for empId: {}, educationId: {}", empId, educationId);
    }

    @Override
    public void deleteEducation(String empId,String educationId){
        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,AppMessages.ID_NOT_FOUND));

        List<EducationDetails> educationDetailsList = userExpEducation.getEducationDetails();
        if (educationDetailsList == null || educationDetailsList.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Optional<EducationDetails> educationToDelete = educationDetailsList.stream()
                .filter(edu -> edu.getEducationId().equals(educationId))
                .findFirst();

        if (educationToDelete.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,AppMessages.EDUCATION_ID_NOT_FOUND);
        }

        educationToDelete.get().setStatus(Status.DELETED.label);

        userExpEducation.setEducationDetails(educationDetailsList);
        userExpEducationRepository.save(userExpEducation);

    }

    private List<EducationDetails> mapToEducationDetails(String empId, EducationRequestDTO eduDetailsDTO) {

        List<EducationDetails> educationDetailsList = new ArrayList<>();
        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseGet(() -> new UserExpEducation(empId, new ArrayList<>(), new ArrayList<>()));

        String lastSequence = userExpEducation.getEducationDetails().stream()
                .map(EducationDetails::getEducationId)
                .max(String::compareTo)
                .orElse(EDUCATION_SEQUENCE);

        // Iterate through DTO education details
        for (EducationRequestDTO.EduDetails eduDetails : eduDetailsDTO.getEduDetails()) {
            EducationDetails educationDetails = new EducationDetails();

            String educationId = AppUtils.generateUniqueIdExpEdu(lastSequence,3);
            lastSequence = educationId;


            // Set education details from DTO
            educationDetails.setEducationId(educationId);
            educationDetails.setQualification(eduDetails.getQualification());
            educationDetails.setCourse(eduDetails.getCourse());
            educationDetails.setBeginOn(DateUtil.parseDate(eduDetails.getBeginOn()));
            educationDetails.setEndOn(DateUtil.parseDate(eduDetails.getEndOn()));
            educationDetails.setInstitute(eduDetails.getInstitute());
            educationDetails.setPercentageOrCgpa(eduDetails.getPercentageOrCgpa());
            educationDetails.setStatus(Status.ACTIVE.label);

            // Upload file if provided
            if (eduDetails.getFiles() != null && !eduDetails.getFiles().isEmpty()) {
                try {
                    DocumentDetails documentDetails = uploadFileAndGetDocumentDetails(eduDetails.getFiles(), empId);
                    educationDetails.setDocumentDetails(documentDetails);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload file for education details", e);
                }
            }

            educationDetailsList.add(educationDetails);
        }

        return educationDetailsList;
    }

    private DocumentDetails uploadFileAndGetDocumentDetails(MultipartFile file, String empId) throws IOException {
        String folderName = "EDUCATION";
        String fileName = generateFileName(empId, folderName);

        String uploadedFileName = fileService.uploadFile(file, FileType.valueOf(folderName), fileName);

        return new DocumentDetails(folderName, uploadedFileName);
    }

    private String generateFileName(String empId, String folderName) {
        return empId + "_" + folderName;
    }


}
