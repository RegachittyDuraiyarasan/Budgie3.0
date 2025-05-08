package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.exceptions.FieldException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.userinfo.DocumentsFetchDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.userinfo.*;
import com.hepl.budgie.mapper.userinfo.OtherDocumentsMapper;
import com.hepl.budgie.repository.userinfo.OtherDocumentsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.userinfo.OtherDocumentsService;

import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class OtherDocumentsServiceImplementation implements OtherDocumentsService {

    private final OtherDocumentsRepository otherDocumentsRepository;

    private final UserInfoRepository userInfoRepository;

    private final FileService fileService;

    private final OtherDocumentsMapper otherDocumentsMapper;

    private final JWTHelper jwtHelper;

    @Override
    public UserOtherDocuments updateOtherDocuments(Map<String, Object> fields, Map<String, FormFieldsDTO> formFields, String empId) throws IOException {
        userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
        log.info("Found UserInfo for empId {}: Employee exists", empId);

        UserOtherDocuments userOtherDocuments = otherDocumentsRepository.findByEmpId(empId)
                .orElseGet(() -> {
                    UserOtherDocuments newDocuments = new UserOtherDocuments();
                    newDocuments.setEmpId(empId);
                    return newDocuments;
                });

        Documents documents = Optional.ofNullable(userOtherDocuments.getDocuments())
                .orElseGet(Documents::new);

        // Iterate through the form fields and process each file field dynamically
        boolean isFileAttachmentAvailable = false;
        for (Map.Entry<String, Object> entry : fields.entrySet()) {

            if (entry.getValue() instanceof MultipartFile singleFile) {
                PassportPhoto photo = processAndUploadSingleFile(singleFile, entry.getKey(), empId);
                if(photo != null) {
                    isFileAttachmentAvailable = true;
                    log.info("photo {}", photo);
                    if(Boolean.TRUE.equals(formFields.get(entry.getKey()).getMultiple())) {
                        invokeSetterMethod(documents, entry.getKey(), List.of(photo), List.class);
                    }else {
                        invokeSetterMethod(documents, entry.getKey(), photo, PassportPhoto.class);
                    }

                }
            } else if (entry.getValue() instanceof List) {
                isFileAttachmentAvailable = true;
                List<MultipartFile> fileList = (List<MultipartFile>) entry.getValue();
                if(!fileList.isEmpty()) {
                    List<PassportPhoto> photos = processAndUploadMultipleFiles(fileList, entry.getKey(), empId);
                    log.info("photos {}", photos);
                    invokeSetterMethod(documents, entry.getKey(), photos, List.class);
                }
            } else {
                log.warn("Unsupported file type for index: {}", entry.getKey());
                throw new FieldException(AppMessages.UNSUPPORTED_FORMAT, Map.of(entry.getKey(), AppMessages.UNSUPPORTED_FORMAT), Map.of(entry.getKey(), new String[]{""}));
            }
        }
        // Ensure at least one document is uploaded
        if (!isFileAttachmentAvailable) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,AppMessages.FILE_VALIDATION);
        }

        userOtherDocuments.setDocuments(documents);
        UserOtherDocuments updatedDocument = otherDocumentsRepository.save(userOtherDocuments);
        log.info("Updated documents for empId {}: {}", empId, updatedDocument);
        return updatedDocument;
    }

    private PassportPhoto processAndUploadSingleFile(MultipartFile file, String index, String empId) {
        try {
            if(!file.isEmpty()) {
                PassportPhoto photo = new PassportPhoto();
                photo.setSubmittedOn(ZonedDateTime.now());

                String fileName = processAndUploadFile(file, index, empId);
                photo.setFileName(fileName);
                photo.setFolderName(determineFolderName(index));

                return photo;
            }
            return null;
        } catch (IOException e) {
            log.error("Error uploading single file: {}", file.getOriginalFilename(), e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Error uploading file: " + file.getOriginalFilename(), e);
        }
    }

    private List<PassportPhoto> processAndUploadMultipleFiles(List<MultipartFile> fileList, String index,
            String empId) {
        List<PassportPhoto> photos = new ArrayList<>();

        for (MultipartFile file : fileList) {
            try {
                PassportPhoto photo = processAndUploadSingleFile(file, index, empId);
                if(photo != null) {
                    photos.add(photo);
                }
            } catch (ResponseStatusException e) {
                log.error("Error uploading one of the files in the list for index: {}", index, e);
                throw e;
            }
        }

        return photos;
    }

    private <T> void invokeSetterMethod(Documents documents, String index, T value, Class<T> type) {
        String setterName = "set" + Character.toUpperCase(index.charAt(0)) + index.substring(1);
        log.info("setterName = {}", setterName);
        try {
            Method setterMethod = Documents.class.getMethod(setterName, type);
            log.info("Invoking setter method: {}", setterName);

            // Invoke the setter dynamically
            setterMethod.invoke(documents, value);
        } catch (NoSuchMethodException e) {
            log.error("Setter method not found: {}", setterName, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No setter method found for field: " + setterName,
                    e);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Error invoking setter method: {}", setterName, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error invoking setter for field: " + setterName,
                    e);
        }
    }

    private String processAndUploadFile(MultipartFile file, String index, String empId) throws IOException {
        String formattedIndex = index.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();

        FileType fileType = FileType.valueOfFolderName(formattedIndex);

        if (fileType != null) {
            log.info("Processing file for field '{}', folderName '{}'", formattedIndex, fileType.folderName);

            String newFileName = generateFileName(empId, formattedIndex);
            return fileService.uploadFile(file, fileType, newFileName);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown file type for field: " + formattedIndex);
        }
    }

    private String generateFileName(String empId, String baseName) {
        return empId + "_" + baseName;
    }

    private String determineFolderName(String index) {
        log.info("Folder Index {}", index);
        String formattedIndex = index.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();

        FileType fileType = FileType.valueOfFolderName(formattedIndex);
        if (fileType != null) {
            return fileType.folderName;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FOLDER_NOT_FOUND + formattedIndex);
        }
    }

    @Override
    public List<DocumentsFetchDTO> getFileNameByEmpId(String empId) {
        return otherDocumentsRepository.findByEmpId(empId)
                .map(UserOtherDocuments::getDocuments)
                .map(this::mapDocumentsToDTOList)
                .orElse(List.of());
    }

    private List<DocumentsFetchDTO> mapDocumentsToDTOList(Documents documents) {
        AtomicInteger idCounter = new AtomicInteger(1);

        return Stream.concat(
                Stream.of(
                                new AbstractMap.SimpleEntry<>("PASSPORT_PHOTO", documents.getPassportPhoto()),
                                new AbstractMap.SimpleEntry<>("RESUME", documents.getResume()),
                                new AbstractMap.SimpleEntry<>("RELIEVING_LETTER", documents.getRelievingLetter()),
                                new AbstractMap.SimpleEntry<>("VACCINATION", documents.getVaccination()),
                                new AbstractMap.SimpleEntry<>("BANK_PASSBOOK", documents.getBankPassbook()),
                                new AbstractMap.SimpleEntry<>("BLOOD_GROUP_PROOF", documents.getBloodGroupProof()),
                                new AbstractMap.SimpleEntry<>("DATE_OF_BIRTH_PROOF", documents.getDateOfBirthProof()),
                                new AbstractMap.SimpleEntry<>("PAN", documents.getPan()),
                                new AbstractMap.SimpleEntry<>("AADHAAR_CARD", documents.getAadhaarCard()),
                                new AbstractMap.SimpleEntry<>("SIGNATURE", documents.getSignature())
                        ).filter(entry -> entry.getValue() != null) // Remove null values
                        .map(entry -> {
                            DocumentsFetchDTO dto = otherDocumentsMapper.mapToDTO(entry.getValue());
                            dto.setId(idCounter.getAndIncrement());
                            dto.setFolderName(entry.getKey());
                            return dto;
                        }),

                Optional.ofNullable(documents.getPayslips())
                        .orElse(List.of())  // Handle null payslips
                        .stream()
                        .map(payslip -> {
                            DocumentsFetchDTO dto = otherDocumentsMapper.mapToDTO(payslip);
                            dto.setId(idCounter.getAndIncrement());
                            dto.setFolderName("PAYSLIPS");
                            return dto;
                        })
        ).collect(Collectors.toList());
    }
}
