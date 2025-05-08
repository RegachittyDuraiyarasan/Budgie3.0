package com.hepl.budgie.config.annotation.validator;

import com.hepl.budgie.config.annotation.ValidReimbursement;
import com.hepl.budgie.dto.payroll.ReimbursementApprovedDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StatusValidator implements ConstraintValidator<ValidReimbursement, ReimbursementApprovedDTO> {

    @Override
    public boolean isValid(ReimbursementApprovedDTO dto, ConstraintValidatorContext context) {
        if (dto.getStatus() == null) {
            return true; // Let @NotNull handle this case
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation(); // Disable default error messages

        if (dto.getStatus() == 0) {
            if (dto.getRemark() == null || dto.getRemark().trim().isEmpty()) {
                context.buildConstraintViolationWithTemplate("Remark is required when status is 0")
                        .addPropertyNode("remark")
                        .addConstraintViolation();
                isValid = false;
            }
            if (dto.getApprovedBillAmount() != null && dto.getApprovedBillAmount() > 0) {
                context.buildConstraintViolationWithTemplate("Approved Bill Amount must be 0 when status is 0")
                        .addPropertyNode("approvedBillAmount")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}
