package com.hepl.budgie.controller.companypolicy;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyDto;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyResponseDto;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyUpdateDTo;
import com.hepl.budgie.dto.documentInfo.DocumentDTO;
import com.hepl.budgie.dto.documentInfo.ResponseDocumentDTO;
import com.hepl.budgie.service.companypolicy.CompanyPolicyService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("company/policy")
@Slf4j
@AllArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CompanyPolicyController {
    private final CompanyPolicyService companyPolicyService;
    private final Translator translator;
      
    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> uploadDocument(
            @ModelAttribute @Valid CompanyPolicyDto companyPolicyDto) throws IOException {
                companyPolicyService.createCompanyPolicy(companyPolicyDto);
        return GenericResponse.success(translator.toLocale(AppMessages.COMPANY_POLICY_ADDED_SUCCESSFULLY));
    }

    @GetMapping("/all-policy")
    public GenericResponse<List<CompanyPolicyResponseDto>> getallCompanyPolicy() {
        List<CompanyPolicyResponseDto> companyPolicyList = companyPolicyService.getallCompanyPolicy();
        return GenericResponse.success(companyPolicyList);
    }

    @PutMapping("/status")
   public GenericResponse<String> updateStatus(
        @RequestParam String policyCategory,
        @RequestParam String comDocDetailsId) {
    companyPolicyService.updateCompanyPolicyStatus(policyCategory, comDocDetailsId);
    return GenericResponse.success(translator.toLocale(AppMessages.STATUS_UPDATED_SUCCESSFULLY));
  }

  @PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
public  GenericResponse<String> updateCompanyPolicy(
        @RequestParam String policyCategory,
        @RequestParam String comDocDetailsId,
        @ModelAttribute CompanyPolicyUpdateDTo companyPolicyUpdateDto) throws IOException {
    companyPolicyService.updateCompanyPolicy(policyCategory, comDocDetailsId, companyPolicyUpdateDto);
    return GenericResponse.success(translator.toLocale(AppMessages.COMPANY_POLICY_UPDATED_SUCCESSFULLY));
}

@DeleteMapping
public GenericResponse<String> softDeletePolicy(
        @RequestParam String policyCategory,
        @RequestParam String comDocDetailsId) {
    companyPolicyService.softDeleteCompanyPolicy(policyCategory, comDocDetailsId);
    return GenericResponse.success(translator.toLocale(AppMessages.POLICY_DELETED_SUCCESSFULLY));
}


}
