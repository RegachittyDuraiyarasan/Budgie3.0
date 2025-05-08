package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_fbp_component")
public class PayrollFBPComponentMaster {
    @Id
    private String id;
    private String componentId;
    private String componentName;
    private String componentSlug;
    private String status = Status.ACTIVE.label;
    private String orgId;

    public void setComponentName(String componentName) {
        this.componentName = componentName;
        this.componentSlug = generateSlug(componentName);
    }

    private String generateSlug(String name) {
        if (name == null) {
            return null;
        }
        return name.toLowerCase().replaceAll("[^a-z0-9 ]", "").replaceAll("\\s+", "_");
    }

}
