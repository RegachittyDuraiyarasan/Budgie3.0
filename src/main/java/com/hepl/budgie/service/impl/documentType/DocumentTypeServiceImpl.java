package com.hepl.budgie.service.impl.documentType;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.documentInfo.DocumentDTO;
import com.hepl.budgie.dto.documentInfo.DocumentDetailsInfoDto;
import com.hepl.budgie.dto.documentInfo.DocumentInfoDto;
import com.hepl.budgie.dto.documentInfo.FileDetailsDto;
import com.hepl.budgie.dto.documentInfo.ResponseDocumentDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.organization.OrganizationAddDTO;
import com.hepl.budgie.entity.FilePathStruct;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.countriesdetails.Country;
import com.hepl.budgie.entity.documentinfo.DocumentDetailsInfo;
import com.hepl.budgie.entity.documentinfo.DocumentInfo;
import com.hepl.budgie.entity.documentinfo.FileDetails;
import com.hepl.budgie.entity.master.ModuleMaster;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.documentcenter.DocumentCenterMapper;
import com.hepl.budgie.repository.documentinfo.DocumentInfoRepo;
import com.hepl.budgie.repository.master.ModuleMasterSettingsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.documentservice.DocumentService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentTypeServiceImpl implements DocumentService {

    private final ModuleMasterSettingsRepository moduleMasterSettingsRepository;
    private final MongoTemplate mongoTemplate;
    private final UserInfoRepository userInfoRepository;
    private final DocumentInfoRepo documentInfoRepo;
    private final DocumentCenterMapper documentCenterMapper;
    private final FileService fileService;
    private final Translator translator;

    private final JWTHelper jwtHelper;


    @Override
    public void addDocumentTYpe(FormRequest formRequest, String referenceName, String org) {
        log.info("Document Type service implementation");
        Map<String, Object> formFields = formRequest.getFormFields();
        String documentType = (String) formFields.get("documentType");
        String moduleId =(String) formFields.get("moduleType");
        boolean isDuplicate = moduleMasterSettingsRepository.fetchOptions(mongoTemplate, referenceName, org)
                .stream()
                .flatMap(doc -> doc.getOptions().stream())
                .anyMatch(option ->documentType.equals(option.get("documentType")));

                if (isDuplicate) {
                    log.warn("Duplicate documentType detected: {}", documentType);
                    throw new IllegalArgumentException(translator.toLocale(AppMessages.DUPLICATE_DOCUMENT_TYPES));
                }

        Map<String, Object> option = new HashMap<>();
        option.put("moduleId", moduleId);
        option.put("documentType", documentType);

        boolean isAdded = moduleMasterSettingsRepository.addOptions(option, mongoTemplate, referenceName, org);
        if (isAdded) {
            log.info("Document Type added successfully with documentId: {}",  moduleId);
        } else {
            log.error("Failed to add Document Type.");
        }
    }

   

    @Override
    public List<Map<String, Object>> getOptionsByReferenceNameContent(String referenceName, String org) {
        List<ModuleMaster> moduleMasters = moduleMasterSettingsRepository.fetchOptions(mongoTemplate, referenceName,
                org);
        List<Map<String, Object>> extractedOptions = new ArrayList<>();

        for (ModuleMaster module : moduleMasters) {
            if (module.getOptions() != null) {
                extractedOptions.addAll(module.getOptions());
            }
        }
        return extractedOptions;
    }

    @Override
    public List<Map<String, Object>> getAllDocumentTypes(String referenceName, String org) {
        log.info("Fetching all document types...");

        List<Map<String, Object>> documentTypes = moduleMasterSettingsRepository
                .fetchOptions(mongoTemplate, referenceName, org)
                .stream()
                .flatMap(doc -> doc.getOptions().stream())
                .map(option -> {
                    Map<String, Object> documentTypeInfo = new HashMap<>();
                    documentTypeInfo.put("moduleId", option.get("moduleId"));
                    documentTypeInfo.put("documentType", option.get("documentType"));
                    return documentTypeInfo;
                })
                .toList();

        log.info("Total document types fetched: {}", documentTypes.size());
        return documentTypes;
    }

    @Override
    public List<ResponseDocumentDTO> getAllDocumentInfo() {
        log.info("Fetching all document info records...");

        List<ResponseDocumentDTO> userinfoList = documentInfoRepo.getDocumentsWithUserInfo(mongoTemplate,
                jwtHelper.getOrganizationCode());

        log.info("Total document info records fetched: {}", userinfoList);
        return userinfoList;
    }

    @Override
    public void updateDocumentStatus(String moduleId, String empId) {
        log.info("Updating document status for documentId: {} and empId: {}",
                moduleId, empId);

        Optional<DocumentInfo> existingDocInfoOpt = documentInfoRepo.findByEmpId(empId);

        if (existingDocInfoOpt.isEmpty()) {
            log.warn("No document info found for empId: {}", empId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    AppMessages.DOCUMENT_NOT_FOUND);
        }

        DocumentInfo documentInfo = existingDocInfoOpt.get();
        boolean isUpdated = false;

        if (documentInfo.getDocdetails() != null) {
            for (DocumentDetailsInfo docDetails : documentInfo.getDocdetails()) {
                if (docDetails.getModuleId() != null &&
                        docDetails.getModuleId().equals(moduleId)) {

                    if (docDetails.getStatus().equalsIgnoreCase(Status.ACTIVE.label)) {
                        docDetails.setStatus(Status.INACTIVE.label);
                    } else if (docDetails.getStatus().equalsIgnoreCase(Status.INACTIVE.label)) {
                        docDetails.setStatus(Status.ACTIVE.label);
                    }
                    docDetails.setLastModifiedDate(ZonedDateTime.now());
                    isUpdated = true;
                    break;
                }
            }
        }

        if (!isUpdated) {
            log.warn("No document found with documentId: {}", moduleId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    AppMessages.DOCUMENT_NOT_FOUND);
        }

        documentInfoRepo.save(documentInfo);
        log.info("Document status updated successfully for documentId: {}",
        moduleId);
    }

    @Override
    public void updateDocumentInfo(DocumentDTO documentInfoDto) throws IOException {

        Optional<DocumentInfo> existingDocInfoOpt = documentInfoRepo.findByEmpId(documentInfoDto.getEmployeesId());
        if (existingDocInfoOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    translator.toLocale(AppMessages.EMPLOYEE_DOCUMENT_NOT_FOUND));
        }
        DocumentInfo documentInfo = existingDocInfoOpt.get();

        Optional<DocumentDetailsInfo> docDetailOpt = documentInfo.getDocdetails().stream()
                .filter(doc -> Objects.equals(doc.getModuleId(), documentInfoDto.getModuleId()))
                .findFirst();

        if (docDetailOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    translator.toLocale(AppMessages.DOCUMENT_NOT_FOUND));
        }

        DocumentDetailsInfo docDetails = docDetailOpt.get();

        docDetails.setDocumentCategory(documentInfoDto.getDocumentsCategory());
        docDetails.setTitle(documentInfoDto.getDocumentsTitle());
        docDetails.setDescription(documentInfoDto.getDocumentsDescription());
        docDetails.setAcknowledgedType(documentInfoDto.getAcknowledgementType());
        docDetails.setAcknowledgementHeading(documentInfoDto.getAcknowledgementHeading());
        docDetails.setAcknowledgementDescription(documentInfoDto.getAcknowledgementDescription());
        docDetails.setLastModifiedDate(ZonedDateTime.now());

        MultipartFile fileUpload = documentInfoDto.getFileUpload();
        if (fileUpload != null && !fileUpload.isEmpty()) {

            String originalFilename = fileUpload.getOriginalFilename();
            if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.INVALID_FILE_FORMAT));
            }

            String baseFilename = originalFilename.substring(0, originalFilename.lastIndexOf('.'));

            // Compare the base filename with the employeeId provided
            String employeeId = documentInfoDto.getEmployeesId();
            if (!baseFilename.equals(employeeId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.FILENAME_MISMATCH));
            }

            if (!Objects.equals(fileUpload.getContentType(), "application/pdf")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.INVALID_FILE_FORMAT));
            }

            String folderName = "EMPLOYEE_DOCUMENT";
            String fileName = generateFileName(documentInfoDto.getEmployeesId());
            String uploadedFilePath = fileService.uploadFile(fileUpload, FileType.valueOf(folderName), fileName);

            FileDetails fileDetails = docDetails.getFileDetails();
            if (fileDetails == null) {
                fileDetails = new FileDetails(folderName, uploadedFilePath);
            } else {

                if (fileDetails.getFileName() != null) {
                    fileService.deleteFile(fileDetails.getFileName(), FileType.valueOf(fileDetails.getFolderName()));
                }
                fileDetails.setFileName(uploadedFilePath);
            }
            docDetails.setFileDetails(fileDetails);
        }
        documentInfoRepo.save(documentInfo);
    }

    @Override
    public void addDocumentInfo(DocumentDTO documentInfoDto) throws IOException {
        Optional<DocumentInfo> existingDocInfoOpt = documentInfoRepo.findByEmpId(documentInfoDto.getEmployeesId());
        DocumentInfo documentInfo = existingDocInfoOpt.orElse(new DocumentInfo());
        documentInfo.setEmpId(documentInfoDto.getEmployeesId());

        if (documentInfo.getDocdetails() == null) {
            documentInfo.setDocdetails(new ArrayList<>());
        }

        String newDocumentId = documentInfoDto.getModuleId();
        boolean documentExists = documentInfo.getDocdetails().stream()
                .anyMatch(doc -> Objects.equals(doc.getModuleId(), newDocumentId));

        if (documentExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    translator.toLocale(AppMessages.DOCUMENT_ALREADY_EXISTS));
        }

        MultipartFile fileUpload = documentInfoDto.getFileUpload();

        if (fileUpload == null || fileUpload.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, translator.toLocale(AppMessages.FILE_NOT_FOUND));
        }

        String originalFilename = fileUpload.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    translator.toLocale(AppMessages.INVALID_FILE_FORMAT));
        }

        String fileNameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        if (!fileNameWithoutExt.equals(documentInfoDto.getEmployeesId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    translator.toLocale(AppMessages.FILENAME_MUST_MATCH_EMPID));
        }

        if (!Objects.equals(fileUpload.getContentType(), "application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    translator.toLocale(AppMessages.INVALID_FILE_FORMAT));
        }

        String folderName = "EMPLOYEE_DOCUMENT";
        String fileName = generateFileName(documentInfoDto.getEmployeesId());

        String uploadedFilePath = fileService.uploadFile(fileUpload, FileType.valueOf(folderName), fileName);
        FileDetails fileDetails = new FileDetails(folderName, uploadedFilePath);

        DocumentDetailsInfo docDetails = new DocumentDetailsInfo();
        docDetails.setModuleId(newDocumentId);
        docDetails.setDocumentCategory(documentInfoDto.getDocumentsCategory());
        docDetails.setTitle(documentInfoDto.getDocumentsTitle());
        docDetails.setDescription(documentInfoDto.getDocumentsDescription());
        docDetails.setAcknowledgedType(documentInfoDto.getAcknowledgementType());
        docDetails.setAcknowledgementHeading(documentInfoDto.getAcknowledgementHeading());
        docDetails.setAcknowledgementDescription(documentInfoDto.getAcknowledgementDescription());
        docDetails.setStatus(Status.ACTIVE.label);
        docDetails.setFileDetails(fileDetails);
        ZonedDateTime now = ZonedDateTime.now();
        docDetails.setCreatedDate(now);
        docDetails.setLastModifiedDate(now);
        documentInfo.getDocdetails().add(docDetails);
        documentInfoRepo.save(documentInfo);
    }

    @Override
    public void addBulkDocumentInfo(DocumentDTO documentInfoDto) throws IOException {
        List<MultipartFile> bulkFileUploads = documentInfoDto.getBulkFileUploads();

        if (bulkFileUploads == null || bulkFileUploads.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    translator.toLocale(AppMessages.NO_FILES_UPLOADED_FOR_BULK_UPLOAD));
        }

        for (MultipartFile fileUpload : bulkFileUploads) {
            if (fileUpload == null || fileUpload.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.ONE_OF_THE_UPLOADED_FILES_IS_EMPTY));
            }

            String originalFilename = fileUpload.getOriginalFilename();
            if (originalFilename == null || !originalFilename.contains(".")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.INVALID_FILE_NAME_FORMAT));
            }

            String empId = originalFilename.substring(0, originalFilename.lastIndexOf(".")); // Extract empId
            System.out.println("empId-------" + empId);

            boolean userExists = userInfoRepository.existsByEmpId(empId);
            if (!userExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.EMPLOYEE_ID_NOT_FOUND));
            }

            // Verify filename matches empId
            if (!originalFilename.equals(empId + ".pdf")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.FILENAME_MUST_MATCH_EMPID));
            }

            // Verify content type
            if (!Objects.equals(fileUpload.getContentType(), "application/pdf")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        translator.toLocale(AppMessages.INVALID_FILE_FORMAT_PDF_ONLY));
            }
            // Create a new DTO with extracted empId
            DocumentDTO singleFileDto = new DocumentDTO();
            singleFileDto.setEmployeesId(empId);
            singleFileDto.setModuleId(documentInfoDto.getModuleId());
            singleFileDto.setDocumentsCategory(documentInfoDto.getDocumentsCategory());
            singleFileDto.setDocumentsTitle(documentInfoDto.getDocumentsTitle());
            singleFileDto.setDocumentsDescription(documentInfoDto.getDocumentsDescription());
            singleFileDto.setAcknowledgementType(documentInfoDto.getAcknowledgementType());
            singleFileDto.setAcknowledgementHeading(documentInfoDto.getAcknowledgementHeading());
            singleFileDto.setAcknowledgementDescription(documentInfoDto.getAcknowledgementDescription());
            singleFileDto.setFileUpload(fileUpload);

            addDocumentInfo(singleFileDto); // Use the existing single file upload logic
        }
    }

    @Override
    public void DeleteDocumentInfo(String moduleId, String empId) {
        log.info("Soft deleting document with documentId: {} and empId: {}", moduleId, empId);

        Optional<DocumentInfo> existingDocInfoOpt = documentInfoRepo.findByEmpId(empId);

        if (existingDocInfoOpt.isEmpty()) {
            log.warn("No document info found for empId: {}", empId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, translator.toLocale(AppMessages.FILE_NOT_FOUND));
        }

        DocumentInfo documentInfo = existingDocInfoOpt.get();
        boolean isDeleted = false;

        if (documentInfo.getDocdetails() != null) {
            for (DocumentDetailsInfo docDetails : documentInfo.getDocdetails()) {
                if (docDetails.getModuleId() != null && docDetails.getModuleId().equals(moduleId)) {
                    docDetails.setStatus(Status.DELETED.label);
                    docDetails.setLastModifiedDate(ZonedDateTime.now());
                    isDeleted = true;
                    break;
                }
            }
        }

        if (!isDeleted) {
            log.warn("No document found with documentId: {}",moduleId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.DOCUMENT_NOT_FOUND);
        }

        documentInfoRepo.save(documentInfo);
        log.info("Document soft deleted successfully for documentId: {}", moduleId);
    }

    private String generateFileName(String empId) {
        return empId;
    }
}