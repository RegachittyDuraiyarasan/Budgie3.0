package com.hepl.budgie.entity.payroll;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "payroll_it_scheme")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollITScheme {

    private String id;
    private String type;
    private String orgId;
    private String description;
    private String schemeExists;
    private List<ITScheme> schemes;
    private String status;
    
}
