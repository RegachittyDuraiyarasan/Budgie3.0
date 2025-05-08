package com.hepl.budgie.utils;

import com.hepl.budgie.config.i18n.Translator;
import com.mongodb.bulk.BulkWriteUpsert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class BulkUpsertUtil {
    private final Translator translator;

    public List<Map<String, String>> processBulkUpsertResults(List<BulkWriteUpsert> upsert,
            List<Map<String, Object>> data, String column) {


        List<Map<String, String>> dbResult = new ArrayList<>();
        Set<String> insertedEmpIds = upsert.stream()
                .map(dbUpsert -> data.get(dbUpsert.getIndex()).get(column).toString())
                .collect(Collectors.toSet());

        for (Map<String, Object> value : data) {
            String colValue = value.get(column).toString();
            Map<String, String> objectMap = new LinkedHashMap<>();
            objectMap.put("row", value.get("row").toString());
            objectMap.put(column, colValue);
            objectMap.put("column", "-");

            if (insertedEmpIds.contains(colValue)) {
                objectMap.put("message", translator.toLocale(AppMessages.DATA_INSERTED));
            } else {
                objectMap.put("message", translator.toLocale(AppMessages.DATA_UPDATED));
            }
            dbResult.add(objectMap);
        }

        return dbResult;
    }
}
