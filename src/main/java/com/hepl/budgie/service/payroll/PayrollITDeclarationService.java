package com.hepl.budgie.service.payroll;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hepl.budgie.dto.payroll.HraDTO;
import com.hepl.budgie.dto.payroll.LetOutDTO;
import com.hepl.budgie.dto.payroll.PayrollDeclarationDTO;
import com.hepl.budgie.dto.payroll.PayrollRequestDTO;
import com.hepl.budgie.dto.payroll.PreviousEmploymentTaxDTO;
import com.hepl.budgie.dto.payroll.SchemeUpdateDTO;
import com.hepl.budgie.entity.payroll.FamilyList;
import com.hepl.budgie.entity.payroll.PayrollITDeclaration;

@Service
public interface PayrollITDeclarationService {

    List<Map<String, String>> getEmployeesByPayrollRoleType();

    List<PayrollITDeclaration> releasePayrollITDeclaration(PayrollRequestDTO release);

    List<PayrollITDeclaration> getPayrollReReleaseList();

    List<PayrollITDeclaration> reReleasePayrollITDeclaration(PayrollRequestDTO release);

    PayrollDeclarationDTO getEmployeePayrollDeclaration();

    PayrollITDeclaration updateRegime(String regime);

    PayrollITDeclaration updateSchemes(SchemeUpdateDTO schemes);

    PayrollITDeclaration updateHra(String planId, HraDTO hra);

    PayrollITDeclaration updateLetOut(String planId, LetOutDTO itLetOut);

    PayrollITDeclaration updateMetro(String planId, String metro);

    PayrollITDeclaration updatePreviousEmployee(String planId, PreviousEmploymentTaxDTO employeeTax);

    PayrollITDeclaration draftPreviousEmployee(String planId, PreviousEmploymentTaxDTO employeeTax);

    PayrollITDeclaration updateFamilies(String planId, List<FamilyList> families);
    
}
