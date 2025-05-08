package com.hepl.budgie.entity.role;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "m_roles")
@EqualsAndHashCode(callSuper = false)
public class Roles extends AuditInfo {

    @Id
    private String id;
    private String roleName;
    private String roleDescription;
    private List<Permissions> permissions;
    private String status;
}
