package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.userinfo.ExperienceDTO;
import com.hepl.budgie.dto.userinfo.ExperienceRequestDTO;
import com.hepl.budgie.dto.userinfo.UpdateExperienceDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.userinfo.DocumentDetails;
import com.hepl.budgie.entity.userinfo.ExperienceDetails;
import com.hepl.budgie.entity.userinfo.UserExpEducation;
import com.hepl.budgie.mapper.userinfo.ExperienceInfoMapper;
import com.hepl.budgie.repository.userinfo.UserExpEducationRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.userinfo.ExperienceInfoService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import com.hepl.budgie.utils.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExperienceInfoServiceImplementation implements ExperienceInfoService {

    private final UserInfoRepository userInfoRepository;

    private final UserExpEducationRepository userExpEducationRepository;

    private final ExperienceInfoMapper experienceInfoMapper;

    private final FileService fileService;

    private  final Translator translator;

    private static final String EXPERIENCE_SEQUENCE = "EX000";

    @Override
    public void addExperience(String empId,ExperienceRequestDTO expDetailsDTO, List<MultipartFile> files) {
        // Validate if the employee exists
        userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, translator.toLocale(AppMessages.RESOURCE_NOT_FOUND)));
        log.info("Found UserInfo for empId {}: Employee exists", empId);

        // Fetch or create UserExpEducation
        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseGet(() -> {
                    UserExpEducation newExperience = new UserExpEducation();
                    newExperience.setEmpId(empId);
                    newExperience.setExperienceDetails(new ArrayList<>());
                    return newExperience;
                });

        // Convert DTO to Entity List
        List<ExperienceDetails> addExpList = mapToExperienceDetails(empId, expDetailsDTO);

        // Filter out duplicate experiences
        List<String> existingExpIds = userExpEducation.getExperienceDetails().stream()
                .map(ExperienceDetails::getExperienceId)
                .toList();

        List<ExperienceDetails> filteredList = addExpList.stream()
                .filter(exp -> !existingExpIds.contains(exp.getExperienceId()))
                .toList();

        userExpEducation.getExperienceDetails().addAll(filteredList);

        userExpEducationRepository.save(userExpEducation);

        log.info("Successfully added experiences for empId {}", empId);
    }

    @Override
    public List<ExperienceDTO> getExperience(String empId) {
        boolean existsInUserInfo = userInfoRepository.existsByEmpId(empId);

        Optional<UserExpEducation> userExpEducationOpt = userExpEducationRepository.findByEmpId(empId);

        if (!existsInUserInfo && userExpEducationOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND);
        }

        // If empId exists in userInfo but not in userExpEducation, return an empty list
        if (userExpEducationOpt.isEmpty()) {
            return Collections.emptyList();
        }

        // Extract experience details
        List<ExperienceDetails> experienceDetailsList = userExpEducationOpt.get().getExperienceDetails();

        // If experienceDetailsList is empty, return an empty list instead of throwing an error
        if (experienceDetailsList == null || experienceDetailsList.isEmpty()) {
            return Collections.emptyList();
        }

        List<ExperienceDetails> activeExperienceDetails = experienceDetailsList.stream()
                .filter(exp -> "Active".equalsIgnoreCase(exp.getStatus()))
                .toList();

        return experienceInfoMapper.mapToDTO(activeExperienceDetails);
    }


    @Override
    public void updateExperience(String empId,String experienceId, UpdateExperienceDTO updateExperienceDTO) {
        log.info("Experience update request: {}", updateExperienceDTO);

        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        if (userExpEducation.getExperienceDetails() == null || userExpEducation.getExperienceDetails().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        boolean existingPresentOn = userExpEducation.getExperienceDetails().stream()
                .anyMatch(exp -> exp.getPresentOn() && !Status.DELETED.label.equals(exp.getStatus()));

        // Find the experience record to update
        ExperienceDetails existingExpDetail = userExpEducation.getExperienceDetails().stream()
                .filter(exp -> experienceId.equals(exp.getExperienceId()) && "Active".equalsIgnoreCase(exp.getStatus()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EXPERIENCE_ID_NOT_FOUND));

        log.info("Found existing experience details: {}", existingExpDetail);

        // Validate request data
        if (updateExperienceDTO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid experience data");
        }

        log.info("Update details received: {}", updateExperienceDTO);

        if (Boolean.TRUE.equals(updateExperienceDTO.getPresentOn())) {
            if (existingPresentOn && !existingExpDetail.getPresentOn()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EXPERIENCE_PRESENT_ON);
            }
        }

        // Update the existing experience details
        existingExpDetail.setJobTitle(updateExperienceDTO.getJobTitle());
        existingExpDetail.setCompanyName(updateExperienceDTO.getCompanyName());
        existingExpDetail.setBeginOn(DateUtil.parseDate(updateExperienceDTO.getBeginOn()));
        existingExpDetail.setPresentOn(updateExperienceDTO.getPresentOn());
        updateExperienceDTO.setStatus(existingExpDetail.getStatus());

        // Handle `presentOn` logic
        if (Boolean.TRUE.equals(updateExperienceDTO.getPresentOn())) {
            existingExpDetail.setEndOn(null);
            existingExpDetail.setDocumentDetails(null);
        } else {
            existingExpDetail.setEndOn(DateUtil.parseDate(updateExperienceDTO.getEndOn()));
        }

        // Handle file upload (only if `presentOn` is false)
        if (Boolean.FALSE.equals(updateExperienceDTO.getPresentOn())) {
            try {
                existingExpDetail.setDocumentDetails(
                        (updateExperienceDTO.getFiles() != null && !updateExperienceDTO.getFiles().isEmpty())
                                ? uploadFileAndGetDocumentDetails(updateExperienceDTO.getFiles(), empId) // New file → Upload
                                : existingExpDetail.getDocumentDetails() // No new file → Retain existing or null
                );
            } catch (IOException e) {
                log.error("File upload failed for authenticatedEmpId {}: {}", empId, e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File upload failed");
            }
        }

        userExpEducationRepository.save(userExpEducation);
        log.info("Successfully updated experience for authenticatedEmpId: {}, experienceId: {}", empId, experienceId);
    }

    @Override
    public void deleteExperience(String empId,String experienceId){
        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,AppMessages.ID_NOT_FOUND));
        List<ExperienceDetails> experienceDetailsList = userExpEducation.getExperienceDetails();
        if (experienceDetailsList == null || experienceDetailsList.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Optional<ExperienceDetails> experienceToDelete = experienceDetailsList.stream()
                .filter(exp -> exp.getExperienceId().equals(experienceId))
                .findFirst();

        if (experienceToDelete.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,AppMessages.EXPERIENCE_ID_NOT_FOUND);
        }

        experienceToDelete.get().setStatus(Status.DELETED.label);

        userExpEducation.setExperienceDetails(experienceDetailsList);
        userExpEducationRepository.save(userExpEducation);

    }

    private List<ExperienceDetails> mapToExperienceDetails(String empId, ExperienceRequestDTO expDetailsDTO) {

        List<ExperienceDetails> experienceDetailsList = new ArrayList<>();
        UserExpEducation userExpEducation = userExpEducationRepository.findByEmpId(empId)
                .orElseGet(() -> new UserExpEducation(empId, new ArrayList<>(), new ArrayList<>()));

        // Check if any existing experience already has presentOn = true
        boolean existingPresentOn = userExpEducation.getExperienceDetails().stream()
                .anyMatch(exp -> exp.getPresentOn() && !Status.DELETED.label.equals(exp.getStatus()));

        String lastSequence = userExpEducation.getExperienceDetails().stream()
                .map(ExperienceDetails::getExperienceId)
                .max(String::compareTo)
                .orElse(EXPERIENCE_SEQUENCE);

        // Iterate through DTO experience details
        for (ExperienceRequestDTO.ExpDetail expDetail : expDetailsDTO.getExpDetails()) {
            ExperienceDetails experienceDetails = new ExperienceDetails();

            String experienceId = AppUtils.generateUniqueIdExpEdu(lastSequence, 3);
            lastSequence = experienceId;


            // Set experience details from DTO
            experienceDetails.setExperienceId(experienceId);
            experienceDetails.setJobTitle(expDetail.getJobTitle());
            experienceDetails.setCompanyName(expDetail.getCompanyName());
            experienceDetails.setBeginOn(DateUtil.parseDate(expDetail.getBeginOn()));
            experienceDetails.setPresentOn(expDetail.getPresentOn());

            // Enforce only one `presentOn = true`
            if (Boolean.TRUE.equals(expDetail.getPresentOn())) {
                if (existingPresentOn) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.EXPERIENCE_PRESENT_ON);
                }
                existingPresentOn = true;
            }

            if (expDetail.getPresentOn() != null && expDetail.getPresentOn()){

                experienceDetails.setEndOn(null);
                experienceDetails.setDocumentDetails(null);
                expDetail.setFiles(null);
            } else {
                experienceDetails.setEndOn(DateUtil.parseDate(expDetail.getEndOn()));
            }
            experienceDetails.setStatus(Status.ACTIVE.label);

            // Handle file upload and associate with DocumentDetails only if presentOn is not true
            if (expDetail.getFiles() != null && !expDetail.getFiles().isEmpty() && !expDetail.getPresentOn()) {
                try {
                    DocumentDetails documentDetails = uploadFileAndGetDocumentDetails(expDetail.getFiles(), empId);
                    log.info("documentDetails {}",documentDetails);
                    experienceDetails.setDocumentDetails(documentDetails);
                } catch (IOException e) {
                    log.error("File upload failed for empId {}: {}", empId, e.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed");
                }
            }

            experienceDetailsList.add(experienceDetails);
        }

        return experienceDetailsList;
    }

    private DocumentDetails uploadFileAndGetDocumentDetails(MultipartFile file, String empId) throws IOException {
        String folderName = "EXPERIENCE";
        String fileName = generateFileName(empId, folderName);

        String uploadedFileName = fileService.uploadFile(file, FileType.valueOf(folderName), fileName);

        return new DocumentDetails(folderName, uploadedFileName);
    }

    private String generateFileName(String empId, String folderName) {
        return empId + "_" + folderName;
    }

}
