package com.hepl.budgie.service.impl.documentType;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.documentInfo.DocumentCenterResponseReportDTo;
import com.hepl.budgie.dto.documentInfo.DocumentResponseReportDTO;
import com.hepl.budgie.dto.documentInfo.ResponseDocumentDTO;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.documentinfo.DocumentCenterReportRepo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.documentservice.DocumentCenterServiceReport;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCenterReportServiceimpl implements
        DocumentCenterServiceReport {

    private final DocumentCenterReportRepo documentCenterReportRepo;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final UserInfoRepository userInfoRepository;

    @Override
    public void addDocumentCenterReport(DocumentCenterResponseReportDTo documentcenterReport) {
        log.info("Add document center report");
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        documentCenterReportRepo.saveOrUpdate(documentcenterReport, mongoTemplate, authUser);
    }
   
    @Override
    public List<DocumentResponseReportDTO> getDocumentReport(){
        List<DocumentResponseReportDTO> documentCenterResponseReportDTo = documentCenterReportRepo.getDocumentReport(jwtHelper.getOrganizationCode(), mongoTemplate);
        return documentCenterResponseReportDTo;
    }
  
   
}
