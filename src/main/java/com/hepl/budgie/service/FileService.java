package com.hepl.budgie.service;

import java.io.IOException;
import java.net.MalformedURLException;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.entity.FileType;

public interface FileService {
                  
    String uploadFile(MultipartFile files, FileType fileType,String fileName) throws IOException;

    String uploadFile(byte[] fileByte, FileType fileType, String fileName) throws IOException;

    void init();

    Resource loadAsResource(String filename, FileType fileType) throws MalformedURLException;

    void moveToBin(String filename, FileType fileType) throws IOException;

    void deleteFile(String filename, FileType fileType) throws IOException;

}
