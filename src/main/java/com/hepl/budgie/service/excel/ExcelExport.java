package com.hepl.budgie.service.excel;

import com.hepl.budgie.dto.excel.HeaderList;

import java.util.List;
import java.util.Map;

public interface ExcelExport {
    List<HeaderList> prepareHeaders();
    default List<List<String>> prepareDataRows() {
        return List.of();
    }
    default List<ExcelBuilder.DropdownConfig> prepareDropdowns() {
        return List.of();
    }
}
