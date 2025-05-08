package com.hepl.budgie.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "m_financial_year")
@AllArgsConstructor
@NoArgsConstructor
public class FinancialYear {
    @Id
    private String id;
    @NotBlank(message = "{validation.error.notBlank}")
    private String startMonthYear;
    private String endMonthYear;
    private String country;
}
