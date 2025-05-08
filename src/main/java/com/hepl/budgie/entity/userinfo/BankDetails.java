package com.hepl.budgie.entity.userinfo;

import java.time.ZonedDateTime;

import lombok.Data;

@Data
public class BankDetails {
    private String accountHolderName;
    private String bankName;
    private String branchName;
    private String accountNumber;
    private String confirmAccountNumber;
    private String accountMobileNumber;
    private String ifscCode;
    private String upiId;
    private ZonedDateTime startDate;
    private Boolean status;
}
