package com.hepl.budgie.entity.settings;

import java.io.ObjectInputFilter.Status;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "m_holidays")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Holiday extends AuditInfo {
    @Id
    private String id; 
    private String occasion;
    private String restrictedHoliday;
    private boolean allState;
    private boolean allLocation;
    private List<String> stateList;
    private List<String> locationList;
    private String discription; 
    private String file;
    private LocalDate date;
    private String day;
    private String status;
    private boolean isRestricted;
}
