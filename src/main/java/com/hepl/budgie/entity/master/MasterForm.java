package com.hepl.budgie.entity.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "m_forms")
public class MasterForm {
    @Id
    private String id;
    private String formName;
    private int maxRows;
    private String formType;
    private boolean status;
    private List<MasterFormFields> formFields;
    private List<Workflow> workflow;
}
