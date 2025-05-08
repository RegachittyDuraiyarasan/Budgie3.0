package com.hepl.budgie.entity.separation;

import lombok.Data;

@Data
public class SeparationBankAccDetails {
    private String accountHolderName;
    private String accountNumber;
    private String confirmAccountNumber;
    private String bankName;
    private String ifscCode;
    private String accountMobileNumber;
    private String branchName;
    private String chequeLeaf;
    private String upiID;
    private String uanNumber;
}
