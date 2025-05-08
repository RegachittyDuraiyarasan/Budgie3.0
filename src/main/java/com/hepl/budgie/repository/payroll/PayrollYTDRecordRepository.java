package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.entity.payroll.PayrollArrears;
import com.hepl.budgie.entity.payroll.PayrollYTDRecord;
import com.mongodb.bulk.BulkWriteResult;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface PayrollYTDRecordRepository extends MongoRepository<PayrollYTDRecord, String> {
    static final String COLLECTION_NAME = "payroll_t_ytd_record_";

    default BulkWriteResult bulkUpsert(MongoTemplate mongoTemplate, String orgId, List<PayrollYTDRecord> dtoList) {

        BulkOperations bulkOperations = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED,
                PayrollYTDRecord.class,
                COLLECTION_NAME + orgId);

        // Delete existing records for Emp ID and Payroll Month
        Query deleteQuery = new Query(Criteria.where("empId").in(dtoList.stream().map(PayrollYTDRecord::getEmpId).toList())
                .and("payrollMonth").in(dtoList.stream().map(PayrollYTDRecord::getPayrollMonth).toList()));
        mongoTemplate.remove(deleteQuery, PayrollYTDRecord.class, COLLECTION_NAME + orgId);

        // Insert new records
        bulkOperations.insert(dtoList);

        return bulkOperations.execute();

    }

    default void insert(MongoTemplate mongoTemplate, String orgId, PayrollYTDRecord ytdRecord) {
        Query deleteQuery = new Query(Criteria.where("empId").in(ytdRecord.getEmpId())
                .and("payrollMonth").in(ytdRecord.getPayrollMonth()));
        mongoTemplate.remove(deleteQuery, PayrollYTDRecord.class, COLLECTION_NAME + orgId);

        mongoTemplate.save(ytdRecord, COLLECTION_NAME + orgId);
    }
}
