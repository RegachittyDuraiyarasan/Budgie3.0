package com.hepl.budgie.entity.payroll;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FamilyList {

    private String name;
    private String relation;
    private LocalDate dob;
    private int age;
    
}
