package com.hepl.budgie.entity.documentinfo;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "t_emp_doc_center")
@Data
public class DocumentInfo  {
    @Id
    private String id;
    private String empId;
    private List<DocumentDetailsInfo> docdetails;
}
        