package com.hepl.budgie.entity.payroll;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollHra {

    private ZonedDateTime from;
    private ZonedDateTime to;
    private long rent;
    private long totalRent;
    private String landlord;
    private String houseNumber;
    private String street;
    private String city;
    private String pincode;
    private String landlordName;
    private String landlordPan;
    private String landlordHouseNumber;
    private String landlordStreet;
    private String landlordCity;
    private String landlordPincode;
    private String adminRemarks;
    private String empRemarks;
    private long approvedAmount;
    private String status;

}
