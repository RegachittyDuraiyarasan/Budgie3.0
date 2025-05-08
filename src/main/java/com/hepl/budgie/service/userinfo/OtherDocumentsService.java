package com.hepl.budgie.service.userinfo;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.userinfo.DocumentsFetchDTO;
import com.hepl.budgie.entity.userinfo.UserOtherDocuments;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface OtherDocumentsService {
    UserOtherDocuments updateOtherDocuments(Map<String, Object> fields, Map<String, FormFieldsDTO> formFields,String empId) throws IOException;

    List<DocumentsFetchDTO> getFileNameByEmpId(String empId);

}
