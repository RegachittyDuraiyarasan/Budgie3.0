package com.hepl.budgie.entity.payroll;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "payroll_it_declaration")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollITDeclaration {

    @Id
    private String id;
    private String empId;
    private String financialYear;
    private String regime;
    private String metro;
    private String planId;
    private String status;
    private int consdider;
    private List<SchemeList>section80;
    private List<SchemeList>chapter6;
    private List<SchemeList>medical;
    private List<SchemeList>otherIncome;
    private List<FamilyList>family;
    private PreviousEmploymentTax previousEmployeeTax;
    private List<PayrollHra>hra;
    private PayrollLetOut itLetOut;
    private List<PayrollLetOutProperties> itLetOutProperties;
    private ZonedDateTime endDate;
    
}
