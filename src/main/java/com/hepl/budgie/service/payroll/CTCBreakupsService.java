package com.hepl.budgie.service.payroll;

import com.mongodb.bulk.BulkWriteResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface CTCBreakupsService {

    BulkWriteResult excelImport(List<Map<String, Object>> file);

    List<Map<String, Object>> list();

    List<Map<String, Object>> singleEmpCTC(String empId);

    List<String> dataTableHeaders();

    void initCTCIndexingForOrg(String organisation);
}
