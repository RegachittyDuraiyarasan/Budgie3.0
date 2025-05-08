package com.hepl.budgie.controller.separation;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.separation.EmployeeInfoDTO;
import com.hepl.budgie.dto.separation.EmployeeSeparationDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.separation.Level;
import com.hepl.budgie.entity.separation.SeparationExitInfo;
import com.hepl.budgie.entity.separation.SeparationInfo;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.separation.SeparationService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Manage separation", description = "")
@Slf4j
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/separation")
public class SeparationController {

    private final SeparationService separationService;
    private final JWTHelper jwtHelper;
    private final FileService fileService;

    @GetMapping("/{empId}")
    public GenericResponse<EmployeeInfoDTO> getContact(@PathVariable String empId) {
        EmployeeInfoDTO employeeInfoDTO = separationService.getEmployeeDetails(empId);
        return GenericResponse.success(employeeInfoDTO);
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<EmployeeSeparationDTO> updateOrInsertEmployeeSeparation(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @Valid @ModelAttribute EmployeeSeparationDTO dto) throws IOException {

        UserRef userRef = jwtHelper.getUserRefDetail();
        if (dto.getSeparationBankAccDetails() != null && file != null) {
            String filePath = fileService.uploadFile(file, FileType.SEPARATION, "");
            dto.getSeparationBankAccDetails().setChequeLeaf(filePath);
        }

        EmployeeSeparationDTO updatedDto = separationService.updateOrInsertEmployeeSeparation(
                userRef.getOrganizationCode(), dto);

        return GenericResponse.success(updatedDto);
    }

    @GetMapping("/separationInfo")
    public GenericResponse<List<EmployeeSeparationDTO>> getSeparationData(
            @RequestParam(value = "empId", required = false) String empId,
            @RequestParam(value = "level", required = false) Level level) {
        UserRef userRef = jwtHelper.getUserRefDetail();
        List<EmployeeSeparationDTO> updatedDto = separationService.getSeparationData(userRef.getOrganizationCode(),
                empId, level.label);
        return GenericResponse.success(updatedDto);
    }

    @GetMapping("/for-repoAndRev")
    public GenericResponse<List<?>> getSeparationDataByRepoAndReview(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "level", required = false) Level level) {
        List<?> updatedDto = separationService.getSeparationDataByRepoAndReview(level.label, status);
        return GenericResponse.success(updatedDto);
    }

    @PostMapping(value = "/exitInfo", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<SeparationExitInfo> updateOrInsertEmployeeSeparation(
            @Valid @ModelAttribute SeparationExitInfo dto) {
        SeparationExitInfo updatedDto = separationService.upsertSeparationExitInfo(dto);
        return GenericResponse.success(updatedDto);
    }

    @GetMapping("/exitInfo/{separationId}")
    public GenericResponse<SeparationExitInfo> getSeparationDataByRepoAndReview(
            @PathVariable String separationId) {
        SeparationExitInfo updatedDto = separationService.getSeparationExitInfoBySeparationId(separationId);
        return GenericResponse.success(updatedDto);
    }

    @GetMapping("/relievingLetter/{empId}")
    public ResponseEntity<byte[]> serveFile(@PathVariable String empId) throws IOException {
        log.info("Get empId .. {}", empId);

        byte[] file = separationService.generateRelievingLetter(empId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Relieving.pdf\"")
                .body(file);
    }

    @GetMapping("/exitInfo-separationId's")
    public GenericResponse<List<SeparationInfo>> getUpcomingRelieving() {
        List<SeparationInfo> updatedDto = separationService.getUpcomingRelieving();
        return GenericResponse.success(updatedDto);
    }

    @GetMapping("/cheque/{filename}")
    public ResponseEntity<byte[]> getSeparationFiles(@PathVariable String filename) throws IOException {
        log.info("Get filename .. {}", filename);
        Resource file = fileService.loadAsResource(filename, FileType.SEPARATION);

        if (file == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FILE_NOT_FOUND);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .body(file.getContentAsByteArray());
    }

}
