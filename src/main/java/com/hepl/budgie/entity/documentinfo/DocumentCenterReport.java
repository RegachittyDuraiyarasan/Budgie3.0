package com.hepl.budgie.entity.documentinfo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "documentCenterReport")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentCenterReport {
    @Id
    private String id;
    private String empId;
    private String process;
    private String status;
    private String category;
    private String downloadStatus;
}
