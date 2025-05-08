package com.hepl.budgie.service.userinfo;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfileImageService {
    String uploadPhoto(String empId, MultipartFile photo) throws IOException;
    String bannerImage(String empId, MultipartFile banner) throws IOException;

}
