package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.AccountInformationDTO;
import com.hepl.budgie.entity.userinfo.BankDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
import com.hepl.budgie.entity.userinfo.AccountInformation;

import java.time.ZonedDateTime;
import java.util.Map;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedSourcePolicy = ReportingPolicy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface AccountInformationMapper {

    @Mapping(target = "bankDetails", expression = "java(java.util.List.of(mapBankDetail(formRequest.getFormFields())))")
    @Mapping(target = "uanNo", expression = "java((String) formRequest.getFormFields().get(\"uanNo\"))")
    @Mapping(target = "pfNo", expression = "java((String) formRequest.getFormFields().get(\"pfNo\"))")
    AccountInformation toEntity(FormRequest formRequest);

    default BankDetails mapBankDetail(Map<String, Object> formFields) {
        if (formFields == null) {
            return null;
        }

        String accounNumber = (String) formFields.get("accountNumber");
        String confirmAccountNumber = (String) formFields.get("confirmAccountNumber");
        String ifscCode = (String) formFields.get("ifscCode");

        if (accounNumber != null && !accounNumber.equals(confirmAccountNumber)){
            throw new IllegalArgumentException("Account Number AND Confirm Account Number do not match.");
        }

//        if (accounNumber != null && !accounNumber.matches("\\d{9,18}")){
//            throw new IllegalArgumentException("Account Number must be between 9 to 18 digits.");
//        }
//
//        if (ifscCode != null && !ifscCode.matches("^[A-Z]{4}0[A-Z0-9]{6}$")){
//            throw new IllegalArgumentException("Invalid IFSC Code. It must be 11 characters long: 4 letters, '0'. 6 alphanumeric characters.");
//        }

        BankDetails bankDetails = new BankDetails();
        bankDetails.setAccountHolderName((String) formFields.get("accountHolderName"));
        bankDetails.setBankName((String) formFields.get("bankName"));
        bankDetails.setBranchName((String) formFields.get("branchName"));
        bankDetails.setAccountNumber((String) formFields.get("accountNumber"));
        bankDetails.setConfirmAccountNumber((String) formFields.get("confirmAccountNumber"));
        bankDetails.setAccountMobileNumber((String) formFields.get("accountMobileNumber"));
        bankDetails.setIfscCode((String) formFields.get("ifscCode"));
        bankDetails.setUpiId((String) formFields.get("upiId"));
        bankDetails.setStartDate(getCurrentDate());
        bankDetails.setStatus(true); // Default status if not provided
        return bankDetails;
    }

    default ZonedDateTime getCurrentDate() {
        return ZonedDateTime.now();
    }

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    AccountInformationDTO mapToDTO(AccountInformation accountInformation);
}
