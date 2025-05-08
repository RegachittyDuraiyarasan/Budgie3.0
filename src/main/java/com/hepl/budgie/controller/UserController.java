package com.hepl.budgie.controller;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.UserDTO;
import com.hepl.budgie.dto.userlogin.LoginResponse;
import com.hepl.budgie.dto.userlogin.UserLogin;
import com.hepl.budgie.dto.userlogin.UserSwitchAuth;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.UserService;
import com.hepl.budgie.service.userinfo.UserAuthService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Create and Manage users", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final JWTHelper jwtHelper;
    private final FileService fileService;
    private final UserService userService;
    private final UserAuthService userAuthService;
    private final Translator translator;

    @PostMapping("/auth/login")
    public GenericResponse<LoginResponse> authLogin(@Valid @RequestBody UserLogin userLogin) {
        log.info("Login using employee id and password");

        return GenericResponse.success(userAuthService.authUser(userLogin));
    }

    @PutMapping("/auth/logout")
    public GenericResponse<String> authLogout() {
        log.info("Logout using employee id and password");

        return GenericResponse.success(translator.toLocale(AppMessages.LOGOUT_SUCCESS));
    }

    // @PreAuthorize("@authz.decide(#root, 'Leave', 'Attendance Apply', 'Edit')")
    @PostMapping("/auth/switch")
    public GenericResponse<LoginResponse> switchUserAuth(@Valid @RequestBody UserSwitchAuth switchAuth) {
        log.info("Switch login");

        return GenericResponse.success(userAuthService.switchUser(switchAuth));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> addUser(@Valid @ModelAttribute UserDTO userRequest)
            throws MessagingException, IOException {
        log.info("Adding Users for meeting agenda");

        jwtHelper.getOrganizationCode();
        if (userRequest.getProfilePhoto() != null) {
            fileService.uploadFile(userRequest.getProfilePhoto(), FileType.PROFILE, "");
        }
        userService.saveUser(userRequest);

        return GenericResponse.success("Done");
    }

    @GetMapping("/profile-pic/{filename}")
    public ResponseEntity<byte[]> serveFile(@PathVariable String filename) throws IOException {
        log.info("Get filename .. {}", filename);
        Resource file = fileService.loadAsResource(filename, FileType.PROFILE);

        if (file == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FILE_NOT_FOUND);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file.getContentAsByteArray());
    }

    @GetMapping("/docs/{folderName}/{filename}")
    public ResponseEntity<byte[]> serveDocs(@PathVariable String folderName, @PathVariable String filename)
            throws IOException {
        log.info("Get filename  {}", filename);
        log.info("Get folderName {}", folderName);

        FileType fileType = FileType.valueOfFolderName(folderName);
        log.info("File Type {}", fileType);
        if (fileType == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FOLDER_NOT_FOUND);
        }
        Resource file = fileService.loadAsResource(filename, fileType);

        if (file == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FILE_NOT_FOUND);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file.getContentAsByteArray());
    }

}
