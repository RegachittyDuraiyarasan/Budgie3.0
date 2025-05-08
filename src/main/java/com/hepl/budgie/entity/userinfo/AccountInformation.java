package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountInformation {
    private List<BankDetails> bankDetails;
    private String uanNo;
    private String pfNo;

}
