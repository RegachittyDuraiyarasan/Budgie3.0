package com.hepl.budgie.entity.iiy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;

import com.hepl.budgie.config.auditing.AuditInfo;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "idea_category")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdeaCategory extends AuditInfo {
    @Id
    private String id;
    private String ideaCategoryId;
    private String ideaCategoryName;
    private String status;

}
