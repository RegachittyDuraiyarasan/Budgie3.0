package com.hepl.budgie.service.companypolicy;

import java.io.IOException;
import java.util.List;

import com.hepl.budgie.dto.companypolicy.CompanyPolicyDto;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyResponseDto;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyUpdateDTo;
import com.hepl.budgie.dto.documentInfo.DocumentDTO;
import com.hepl.budgie.dto.documentInfo.ResponseDocumentDTO;

public interface CompanyPolicyService {
   void createCompanyPolicy(CompanyPolicyDto companyPolicyDto)throws IOException;

   List<CompanyPolicyResponseDto> getallCompanyPolicy();

   void updateCompanyPolicyStatus(String policyCategory, String comDocDetailsId);

   void softDeleteCompanyPolicy(String policyCategory, String comDocDetailsId);

   void updateCompanyPolicy(String policyCategory, String comDocDetailsId, CompanyPolicyUpdateDTo companyPolicyUpdateDto)throws IOException;
}
