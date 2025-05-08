package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.entity.FilePathStruct;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.userinfo.ProfileImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileImageServiceImpl implements ProfileImageService {
    private final UserInfoRepository userInfoRepository;
    private final FileService fileService;
    private final JWTHelper jwtHelper;

    @Override
    public String uploadPhoto(String empId, MultipartFile photo) throws IOException {

        if (photo == null || photo.isEmpty()) {
            throw new IllegalArgumentException("Uploaded photo cannot be null or empty");
        }

        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);
        if (userInfoOptional.isEmpty()) {
            throw new RuntimeException("User not found for employee ID: " + empId);
        }

        UserInfo userInfo = userInfoOptional.get();
        String folderName = "PROFILE_PHOTO";
        String fileName = generateFileName(empId, folderName);

        String uploadedFilePath = fileService.uploadFile(photo, FileType.valueOf(folderName), fileName);
        FilePathStruct profilePicture = new FilePathStruct(folderName, uploadedFilePath);
        profilePicture.setFileName(uploadedFilePath);
        profilePicture.setFolderName(folderName);
        userInfo.getSections().setProfilePicture(profilePicture);

        userInfoRepository.save(userInfo);
        return uploadedFilePath;
    }

    @Override
    public String bannerImage(String empId, MultipartFile banner) throws IOException {

        if (banner == null || banner.isEmpty()) {
            throw new IllegalArgumentException("Uploaded photo cannot be null or empty");
        }

        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);
        if (userInfoOptional.isEmpty()) {
            throw new RuntimeException("User not found for employee ID: " + empId);
        }

        UserInfo userInfo = userInfoOptional.get();
        String folderName = "BANNER_IMAGE";
        String fileName = generateFileName(empId, folderName);

        String uploadedFilePath = fileService.uploadFile(banner, FileType.valueOf(folderName), fileName);
        FilePathStruct bannerImage = new FilePathStruct(folderName, uploadedFilePath);
        bannerImage.setFileName(uploadedFilePath);
        bannerImage.setFolderName(folderName);
        userInfo.getSections().setBannerImage(bannerImage);

        userInfoRepository.save(userInfo);
        return uploadedFilePath;
    }

    private String generateFileName(String empId, String folderName) {
        return empId + "_" + folderName;
    }
}
