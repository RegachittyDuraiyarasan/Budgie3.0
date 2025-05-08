// package com.hepl.budgie.service.impl.responseUserDetails;

// import java.io.IOException;

// import org.springframework.data.mongodb.core.MongoTemplate;
// import org.springframework.http.HttpStatus;
// import org.springframework.stereotype.Service;
// import org.springframework.web.server.ResponseStatusException;

// import com.hepl.budgie.dto.responseUserDetails.ResponseUserDetailsDTO;
// import com.hepl.budgie.entity.FileType;
// import
// com.hepl.budgie.repository.responseUserDetails.ResponseUserDetailsRepo;
// import com.hepl.budgie.service.FileService;
// import com.hepl.budgie.service.responseUserDetails.ResponseUserDetailService;
// import com.hepl.budgie.utils.AppMessages;

// import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class ResponseUserDetailsServiceImpl implements
// ResponseUserDetailService {

// private final ResponseUserDetailsRepo responseUserDetailsRepo;
// private final MongoTemplate mongoTemplate;
// private FileService fileService;

// public ResponseUserDetailsServiceImpl(ResponseUserDetailsRepo
// responseUserDetailsRepo,
// MongoTemplate mongoTemplate, FileService fileService) {
// this.responseUserDetailsRepo = responseUserDetailsRepo;
// this.mongoTemplate = mongoTemplate;
// this.fileService = fileService;
// }

// @Override
// public void add(ResponseUserDetailsDTO responseUserDetailsDTO) {
// log.info("adding response user details");

// try {
// if (responseUserDetailsDTO.getIdCard() != null &&
// !responseUserDetailsDTO.getIdCard().isEmpty()) {
// String path = fileService.uploadFile(responseUserDetailsDTO.getIdCard(),
// FileType.USERID_CARD, "");
// responseUserDetailsDTO.setIdCard(path);

// }
// } catch (IOException e) {
// log.error("Error uploading ID card file: {}", e.getMessage(), e);
// throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
// AppMessages.FILE_UPLOAD_ERROR);
// }
// responseUserDetailsRepo.save(responseUserDetailsDTO);
// }

// }
