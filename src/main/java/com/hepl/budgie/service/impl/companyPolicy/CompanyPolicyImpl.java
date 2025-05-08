package com.hepl.budgie.service.impl.companyPolicy;

import java.io.IOException;
import java.io.ObjectInputFilter.Status;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyDto;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyResponseDto;
import com.hepl.budgie.dto.companypolicy.CompanyPolicyUpdateDTo;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.companypolicy.CompanyDocDetails;
import com.hepl.budgie.entity.companypolicy.CompanyPolicy;
import com.hepl.budgie.entity.companypolicy.FileDetails;
import com.hepl.budgie.entity.documentinfo.DocumentDetailsInfo;
import com.hepl.budgie.entity.documentinfo.DocumentInfo;
import com.hepl.budgie.entity.organization.OrganizationMap;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.companypolicy.CompanyPolicyRepo;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.companypolicy.CompanyPolicyService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyPolicyImpl implements CompanyPolicyService {
    private static final String PD_SEQUENCE = "PD00000";
    private final CompanyPolicyRepo companyPolicyRepo;
    private final FileService fileService;
    private final Translator translator;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;

    @Override
    public void createCompanyPolicy(CompanyPolicyDto companyPolicyDto)throws IOException{
        
        Optional<CompanyPolicy> optionalExistingPolicy = companyPolicyRepo.findByPolicyCategory(companyPolicyDto.getPolicyCategory(),mongoTemplate);

        CompanyPolicy companyPolicy;
        if (optionalExistingPolicy.isPresent()) {
            companyPolicy = optionalExistingPolicy.get();
        } else {
            companyPolicy = new CompanyPolicy();
            companyPolicy.setComDocDetails(new ArrayList<>());
        }
         
         MultipartFile fileUpload = companyPolicyDto.getFileUpload();

        if (fileUpload == null || fileUpload.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, translator.toLocale(AppMessages.FILE_NOT_FOUND));
        }

        if (!Objects.equals(fileUpload.getContentType(), "application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    translator.toLocale(AppMessages.INVALID_FILE_FORMAT));
        }
        String folderName = "COMPANY_POLICY";
        String fileName = "companyPolicy";

        String uploadedFilePath = fileService.uploadFile(fileUpload, FileType.valueOf(folderName), fileName);
        FileDetails fileDetails = new FileDetails(folderName,uploadedFilePath);
        CompanyDocDetails companyDocDetails = new CompanyDocDetails();
      
     
        log.info("data-{}", companyPolicyRepo.findTopByOrderByIdDesc());
        String lastId = companyPolicyRepo.findTopByOrderByIdDesc()
                            .map(e -> AppUtils.generateUniqueId(e.getComDocDetails().get(e.getComDocDetails().size()-1).getComDocDetailsId()))
                            .orElse(AppUtils.generateUniqueId(PD_SEQUENCE));
        companyDocDetails.setComDocDetailsId(
            lastId
        );
                System.out.println("123456789---"+AppUtils.generateUniqueId(PD_SEQUENCE)); 
        companyDocDetails.setPolicyCategory(companyPolicyDto.getPolicyCategory());             
        companyDocDetails.setTitle(companyPolicyDto.getTitle());
        companyDocDetails.setDescription(companyPolicyDto.getDescription());
        companyDocDetails.setAcknowledgementType(companyPolicyDto.getAcknowledgementType());
        companyDocDetails.setAcknowledgementHeading(companyPolicyDto.getAcknowledgementHeading());
        companyDocDetails.setAcknowledgementDescription(companyPolicyDto.getAcknowledgementDescription()); 
        companyDocDetails.setStatus("Active");
        companyDocDetails.setFileDetails(fileDetails); 
        companyPolicy.getComDocDetails().add(companyDocDetails);
        ZonedDateTime now = ZonedDateTime.now();
        companyDocDetails.setUploadedOn(now);
        companyDocDetails.setUploadedBy(jwtHelper.getUserRefDetail().getEmpId());
        companyPolicyRepo.save(companyPolicy);
    }

    @Override
    public List<CompanyPolicyResponseDto> getallCompanyPolicy() {
    List<CompanyPolicy> companyPolicies = mongoTemplate.findAll(CompanyPolicy.class);

    if(companyPolicies.isEmpty()){
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,AppMessages.NO_DATA_FOUND);
    }
    
    List<CompanyPolicyResponseDto> responseList = new ArrayList<>();

    for (CompanyPolicy companyPolicy : companyPolicies) {
        for (CompanyDocDetails docDetails : companyPolicy.getComDocDetails()) {
            CompanyPolicyResponseDto dto = new CompanyPolicyResponseDto();
          
            dto.setComDocDetailsId(docDetails.getComDocDetailsId());
            dto.setPolicyCategory(docDetails.getPolicyCategory());
            dto.setTitle(docDetails.getTitle());
            dto.setDescription(docDetails.getDescription());
            dto.setFolderName(docDetails.getFileDetails().getFolderName());
            dto.setFileName(docDetails.getFileDetails().getFileName());
            dto.setStatus(docDetails.getStatus());
            dto.setAcknowledgementType(docDetails.getAcknowledgementType());
            dto.setAcknowledgementHeading(docDetails.getAcknowledgementHeading());
            dto.setAcknowledgementDescription(docDetails.getAcknowledgementDescription());
            responseList.add(dto);
        }
    }
    return responseList;
}

@Override
public void updateCompanyPolicyStatus(String policyCategory, String comDocDetailsId) {
    Optional<CompanyPolicy> optionalPolicy = companyPolicyRepo.findByPolicyCategory(policyCategory, mongoTemplate);

    if (optionalPolicy.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, translator.toLocale(AppMessages.POLICY_CATEGORY_NOT_FOUND));
    }

    CompanyPolicy policy = optionalPolicy.get();
    boolean isUpdated = false;

    for (CompanyDocDetails docDetails : policy.getComDocDetails()) {
        if (docDetails.getComDocDetailsId().equals(comDocDetailsId)) {
            String currentStatus = docDetails.getStatus();

            if ("Active".equalsIgnoreCase(currentStatus)) {
                docDetails.setStatus("InActive");
            } else if ("InActive".equalsIgnoreCase(currentStatus)) {
                docDetails.setStatus("Active");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,translator.toLocale(AppMessages.UNKNOWN_STATUS));
            }

            companyPolicyRepo.save(policy);
            isUpdated = true;
            break;
        }
    }

    if (!isUpdated) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,translator.toLocale(AppMessages.NOT_FOUND_IN_POLICY_CATEGORY));
    }
}

@Override
public void softDeleteCompanyPolicy(String policyCategory, String comDocDetailsId) {
    Optional<CompanyPolicy> optionalPolicy = companyPolicyRepo.findByPolicyCategory(policyCategory, mongoTemplate);

    if (optionalPolicy.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,translator.toLocale(AppMessages.POLICY_CATEGORY_NOT_FOUND) );
    }
    CompanyPolicy policy = optionalPolicy.get();
    boolean isDeleted = false;

    for (CompanyDocDetails docDetails : policy.getComDocDetails()) {
        if (docDetails.getComDocDetailsId().equals(comDocDetailsId)) {
            docDetails.setStatus("Deleted");
            companyPolicyRepo.save(policy);
            isDeleted = true;
            break;
        }
    }

    if (!isDeleted) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, translator.toLocale(AppMessages.NOT_FOUND_IN_POLICY_CATEGORY));
    }
}

@Override
public void updateCompanyPolicy(String policyCategory, String comDocDetailsId, CompanyPolicyUpdateDTo companyPolicyUpdateDto) throws IOException {
    Optional<CompanyPolicy> optionalPolicy = companyPolicyRepo.findByPolicyCategory(policyCategory,mongoTemplate);
    log.info("---------------------policyCategory"+optionalPolicy);

    if (optionalPolicy.isEmpty()) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, translator.toLocale(AppMessages.POLICY_CATEGORY_NOT_FOUND));
    }

    CompanyPolicy companyPolicy = optionalPolicy.get();
    boolean isUpdated = false;

    for (CompanyDocDetails docDetails : companyPolicy.getComDocDetails()) {
        if (docDetails.getComDocDetailsId().equals(comDocDetailsId)) {
            docDetails.setTitle(companyPolicyUpdateDto.getTitle());
            docDetails.setDescription(companyPolicyUpdateDto.getDescription());
            docDetails.setAcknowledgementType(companyPolicyUpdateDto.getAcknowledgementType());
            docDetails.setAcknowledgementHeading(companyPolicyUpdateDto.getAcknowledgementHeading());
            docDetails.setAcknowledgementDescription(companyPolicyUpdateDto.getAcknowledgementDescription());

            MultipartFile newFile = companyPolicyUpdateDto.getFileUpload();

            if (newFile != null && !newFile.isEmpty()) {
                if (!Objects.equals(newFile.getContentType(), "application/pdf")) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            translator.toLocale(AppMessages.INVALID_FILE_FORMAT));
                }

                String folderName = "COMPANY_POLICY";
                String fileName = "companyPolicy";
                String uploadedFilePath = fileService.uploadFile(newFile, FileType.valueOf(folderName), fileName);

                FileDetails newFileDetails = new FileDetails(folderName, uploadedFilePath);
                docDetails.setFileDetails(newFileDetails);
            }

            docDetails.setUploadedOn(ZonedDateTime.now());
            docDetails.setUploadedBy(jwtHelper.getUserRefDetail().getEmpId());

            companyPolicyRepo.save(companyPolicy);
            isUpdated = true;
            break;
        }
    }

    if (!isUpdated) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, translator.toLocale(AppMessages.NOT_FOUND_IN_POLICY_CATEGORY));
    }
}


}




