package com.hepl.budgie.entity.attendancemanagement;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=true)
@Document(collection = "attendance_weekend_policy")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceWeekendPolicy extends AuditInfo{

    @Id
    private String id;
    private String month;
    private List<WeekEnd> week;
       
}
