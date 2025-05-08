package com.hepl.budgie.service.iiy;

import com.mongodb.bulk.BulkWriteResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface HRService {
    ResponseEntity<byte[]> importActivityLists(MultipartFile file) throws IOException;

    BulkWriteResult excelImport(List<Map<String, Object>> validRows);

}
