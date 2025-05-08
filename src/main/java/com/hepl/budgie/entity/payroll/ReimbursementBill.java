package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementBill {

        private String reimbursementId = UUID.randomUUID().toString(); // Auto-generate ID
        private String claimDate;
        private String billDate;
        private String billNo;
        private ReimbursementBill.ReimbursementDocument attachment;
        private double billAmount;
        private double approvedBillAmount = 0;
        private String remarks;
        private String status;
        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        public static class ReimbursementDocument {
            private String folderName;
            private String fileName;
        }

}
