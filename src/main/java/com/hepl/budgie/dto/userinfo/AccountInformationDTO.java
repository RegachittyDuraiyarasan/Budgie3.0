package com.hepl.budgie.dto.userinfo;

import com.hepl.budgie.entity.userinfo.BankDetails;
import lombok.Data;

import java.util.List;

@Data
public class AccountInformationDTO {
    private List<BankDetails> bankDetails;
    private String uanNo;
    private String pfNo;
}
