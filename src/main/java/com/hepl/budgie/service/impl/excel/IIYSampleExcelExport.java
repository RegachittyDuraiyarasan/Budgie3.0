package com.hepl.budgie.service.impl.excel;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.entity.iiy.CourseCategory;
import com.hepl.budgie.repository.iiy.EmployeeRepository;
import com.hepl.budgie.service.excel.ExcelExport;
import com.hepl.budgie.service.impl.iiy.CourseCategoryServiceImpl;
import com.hepl.budgie.service.impl.iiy.EmployeeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Component
public class IIYSampleExcelExport implements ExcelExport {
    private final EmployeeRepository employeeRepository;
    private final MongoTemplate mongoTemplate;
    private final EmployeeServiceImpl iiyEmployeeServiceImpl;
    private final CourseCategoryServiceImpl courseCategoryServiceImpl;
    @Override
    public List<HeaderList> prepareHeaders(){
        List<CourseCategory> courseCategoryList = courseCategoryServiceImpl
                .fetchCourseCategory();
        List<String> courseCategoryNames = courseCategoryList.stream()
                .map(CourseCategory::getCategoryName)
                .collect(Collectors.toList());

        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String"),
//                new HeaderList("Employee_Name", true, "String"),
                new HeaderList("Financial_Year", true, "String"),
//                new HeaderList("Date of Joining", true, "Date"),
//                new HeaderList("Department", false, "String"),
//                new HeaderList("Designation", false, "String"),
//                new HeaderList("Division Head Name", false, "String"),
                new HeaderList("Date", false, "Date")
        ).toList());
        for (String categoryName : courseCategoryNames) {
            headerList.add(new HeaderList(categoryName.replaceAll("\\s+", "_"),false,"Double"));
        }
        headerList.add(new HeaderList("Total_Hours", false,"Double"));

        return headerList;

    }

}
