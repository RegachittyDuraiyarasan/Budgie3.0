package com.hepl.budgie.entity.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "module_m_settings")
public class ModuleMaster {
    @Id
    private String id;
    private String referenceName;
    private List<Map<String, Object>> options;
    
}
