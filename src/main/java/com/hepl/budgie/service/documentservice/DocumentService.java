package com.hepl.budgie.service.documentservice;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.documentInfo.DocumentDTO;
import com.hepl.budgie.dto.documentInfo.DocumentDetailsInfoDto;
import com.hepl.budgie.dto.documentInfo.DocumentInfoDto;
import com.hepl.budgie.dto.documentInfo.ResponseDocumentDTO;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.documentinfo.DocumentInfo;
import com.hepl.budgie.entity.master.ModuleMaster;

public interface DocumentService {

  void addDocumentTYpe(FormRequest formRequest, String referenceName, String org);

  List<Map<String, Object>> getAllDocumentTypes(String referenceName, String org);

  
  List<Map<String, Object>> getOptionsByReferenceNameContent(String referenceName, String org);

  void updateDocumentInfo(DocumentDTO documentInfoDto) throws IOException;

  // List<Map<String, Object>> getAllDocumentInfo();
  List<ResponseDocumentDTO> getAllDocumentInfo();

  void updateDocumentStatus(String moduleId, String empId);

  void DeleteDocumentInfo(String moduleId, String empId);

  void addDocumentInfo(DocumentDTO documentInfoDto) throws IOException;

  void addBulkDocumentInfo(DocumentDTO documentInfoDto) throws IOException;

}
