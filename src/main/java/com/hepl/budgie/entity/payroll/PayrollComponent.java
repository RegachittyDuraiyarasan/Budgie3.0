package com.hepl.budgie.entity.payroll;

import com.hepl.budgie.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payroll_m_component")
public class PayrollComponent {
    @Id
    private String id;
    private String componentId;
    private String componentType;
    private String componentName;
    private String componentSlug;
    private String payType;
    private String compNamePaySlip;
    private boolean compShowInPaySlip;
    private boolean proDataBasisCalc;
    private boolean arrearsCalc;
    private boolean lopCalc;
    private boolean taxCalc;
    private boolean pfCalc;
    private boolean esicCalc;
    private boolean ptCalc;
    private String status;
}
