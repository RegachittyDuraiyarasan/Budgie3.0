package com.hepl.budgie.service.impl.IdCard;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.idCard.GraphicsTeamIdDTO;
import com.hepl.budgie.dto.userinfo.ImageDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.userinfo.*;
import com.hepl.budgie.repository.userinfo.OtherDocumentsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.IdCard.IdCardPhotoService;
import com.hepl.budgie.service.excel.ExcelService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdCardPhotoServiceImpl implements IdCardPhotoService {
    private final UserInfoRepository userInfoRepository;
    private final OtherDocumentsRepository otherDocumentsRepository;
    private final FileService fileService;
    private final JWTHelper jwtHelper;
    private final ExcelService excelService;

    @Override
    public String idCardUpload(String emp, String empId, MultipartFile multipartFile) throws IOException {
        String em = jwtHelper.getUserRefDetail().getEmpId();
        if (!isPngFormat(multipartFile)) {
            throw new IllegalArgumentException("Only PNG format is allowed.");
        }

        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null || !originalFilename.equalsIgnoreCase(empId + ".png")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.INVALID_ID_CARD);
        }
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new IllegalArgumentException("User with empId " + empId + " not found"));
        if (userInfo.getIdCardDetails() == null) {
            userInfo.setIdCardDetails(new IdCardDetails());
        }
        String folderName = "ID_CARD_PHOTO";
        String fileName = generateFileName(empId, folderName);
        log.info("Generated file name: {}", fileName);
        String uploadedFilePath = fileService.uploadFile(multipartFile, FileType.valueOf(folderName), fileName);
        IdPhotoByGraphics idPhoto = new IdPhotoByGraphics();
        idPhoto.setFileName(uploadedFilePath);
        idPhoto.setFolderName(folderName);
        idPhoto.setCreatedBy(em);
        idPhoto.setSubmittedOn(ZonedDateTime.now());
        idPhoto.setPhotoStatusByGraphics(Status.COMPLETED.label);
        userInfo.getIdCardDetails().setIdPhotoByGraphics(idPhoto);
        userInfoRepository.save(userInfo);
        return uploadedFilePath;
    }

    private String generateFileName(String empId, String folderName) {
        return empId + "_" + folderName;
    }

    private boolean isPngFormat(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        return originalFilename != null && originalFilename.toLowerCase().endsWith(".png");
    }

    @Override
    public List<GraphicsTeamIdDTO> graphicsTeamIdDto(String empId, String employeeEmpId,
                                                     String reportingManagerEmpId, String dateOfJoining,
                                                     String result) {
        // Fetch only active users with non-null sections
        List<UserInfo> users = userInfoRepository.findAll().stream()
                .filter(user -> user.getSections() != null)
                .filter(user -> Optional.ofNullable(user.getStatus())
                        .map(status -> status.equalsIgnoreCase("Active"))
                        .orElse(false))
                .collect(Collectors.toList());

        // Fetch all document data
        List<UserOtherDocuments> otherDocuments = otherDocumentsRepository.findAll();

        // Apply filtering
        users = users.stream()
                .filter(user -> employeeEmpId == null || employeeEmpId.isEmpty() || employeeEmpId.equalsIgnoreCase(user.getEmpId()))
                .filter(user -> {
                    String managerId = Optional.ofNullable(user.getSections())
                            .map(Sections::getHrInformation)
                            .map(HrInformation::getPrimary)
                            .map(ReporteeDetail::getManagerId)
                            .orElse(null);
                    return reportingManagerEmpId == null || reportingManagerEmpId.isEmpty()
                            || (managerId != null && managerId.equalsIgnoreCase(reportingManagerEmpId));
                })
                .filter(user -> {
                    String doj = Optional.ofNullable(user.getSections())
                            .map(Sections::getWorkingInformation)
                            .map(WorkingInformation::getDoj)
                            .map(ZonedDateTime::toLocalDate)
                            .map(LocalDate::toString)
                            .orElse(null);
                    return dateOfJoining == null || dateOfJoining.isEmpty()
                            || (doj != null && doj.equals(dateOfJoining));
                })
                .filter(user -> {
                    String idCardStatus = Optional.ofNullable(user.getIdCardDetails())
                            .map(IdCardDetails::getIdPhotoByGraphics)
                            .map(IdPhotoByGraphics::getPhotoStatusByGraphics)
                            .orElse(Status.PENDING.label);
                    return result == null || result.isEmpty() || idCardStatus.equalsIgnoreCase(result);
                })
                .collect(Collectors.toList());

        // Mapping to DTO
        return users.stream().map(user -> {
            GraphicsTeamIdDTO dto = new GraphicsTeamIdDTO();

            String empFirstName = Optional.ofNullable(user.getSections())
                    .map(Sections::getBasicDetails)
                    .map(BasicDetails::getFirstName)
                    .orElse(null);

            dto.setEmployeeCode(empFirstName + " - " + user.getEmpId());

            String managerId = Optional.ofNullable(user.getSections())
                    .map(Sections::getHrInformation)
                    .map(HrInformation::getPrimary)
                    .map(ReporteeDetail::getManagerId)
                    .orElse(null);

            String managerName = userInfoRepository.findByEmpId(managerId)
                    .map(UserInfo::getSections)
                    .map(Sections::getBasicDetails)
                    .map(BasicDetails::getFirstName)
                    .orElse(null);

            dto.setReportingManager(managerName + " - " + (managerId != null ? managerId : ""));

            String doj = Optional.ofNullable(user.getSections())
                    .map(Sections::getWorkingInformation)
                    .map(WorkingInformation::getDoj)
                    .map(ZonedDateTime::toString)
                    .orElse(null);
            dto.setDateOfJoining(doj);

            String photoStatus = Optional.ofNullable(user.getIdCardDetails())
                    .map(IdCardDetails::getIdPhotoByGraphics)
                    .map(IdPhotoByGraphics::getPhotoStatusByGraphics)
                    .orElse(Status.PENDING.label);
            dto.setPhotoStatusByGraphics(photoStatus);

            // Passport photo
            UserOtherDocuments userDoc = otherDocuments.stream()
                    .filter(doc -> doc.getEmpId().equals(user.getEmpId()))
                    .findFirst()
                    .orElse(null);

            PassportPhoto passportPhoto = userDoc != null ? userDoc.getDocuments().getPassportPhoto() : null;

            ImageDTO passportImage = passportPhoto != null
                    ? new ImageDTO(passportPhoto.getFolderName(), passportPhoto.getFileName())
                    : null;

            dto.setViewPassPortPhoto(passportImage);
            dto.setDownloadPassPortPhoto(passportImage);

            // ID card photo
            ImageDTO idCardPhoto = Optional.ofNullable(user.getIdCardDetails())
                    .map(IdCardDetails::getIdPhotoByGraphics)
                    .map(id -> new ImageDTO(id.getFolderName(), id.getFileName()))
                    .orElse(null);

            dto.setIdCardPhoto(idCardPhoto);

            return dto;
        }).collect(Collectors.toList());
    }



    @Override
    public byte[] bulkUpload(String action, List<MultipartFile> files) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.NO_FILES_UPLOAD);
        }
        String authenticateEmpId = jwtHelper.getUserRefDetail().getEmpId();
        List<String[]> data = new ArrayList<>();

        for (MultipartFile file : files) {
            String original = file.getOriginalFilename();

            if (original == null || !original.contains(".")) {
                data.add(new String[]{"", "Invalid filename; skipped"});
                continue;
            }

            String empId = original.substring(0, original.lastIndexOf('.'));

            Optional<UserInfo> userInfo = userInfoRepository.findByEmpId(empId);
            if (userInfo.isEmpty()) {
                data.add(new String[]{empId, "User not found"});
                continue;
            }
            UserInfo user = userInfo.get();

            if (!Status.ACTIVE.label.equalsIgnoreCase(user.getStatus())) {
                data.add(new String[]{empId, "User not active"});
                continue;
            }

            if ("insert".equalsIgnoreCase(action)) {
                IdCardDetails existing = user.getIdCardDetails();
                if (existing != null && existing.getIdPhotoByGraphics() != null) {
                    data.add(new String[]{empId, "File already exists"});
                    continue;
                }
            }

            String folder = "ID_CARD_PHOTO";
            String fileName = generateFileName(empId, folder);
            String path     = fileService.uploadFile(file, FileType.valueOf(folder), fileName);

            IdPhotoByGraphics photo = new IdPhotoByGraphics(path, folder, Status.COMPLETED.label, authenticateEmpId, ZonedDateTime.now());
            IdCardDetails card = user.getIdCardDetails();
            if (card == null) card = new IdCardDetails();
            card.setIdPhotoByGraphics(photo);
            user.setIdCardDetails(card);
            userInfoRepository.save(user);

            data.add(new String[]{empId, (action.equalsIgnoreCase("insert") ? "Inserted" : "Updated")});

        }
        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String"),
                new HeaderList("Message", true, "String")
        ).toList());
        return  excelService.payrollResponseExcel(headerList, data.stream().map(Arrays::asList).toList());
    }


}
