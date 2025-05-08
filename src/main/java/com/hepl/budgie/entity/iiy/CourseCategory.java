package com.hepl.budgie.entity.iiy;

import com.hepl.budgie.config.auditing.AuditInfo;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "course_category")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class CourseCategory extends AuditInfo {
    @Id
    private String id;
    private String categoryId;
    private String categoryName;
    private String status;
}
