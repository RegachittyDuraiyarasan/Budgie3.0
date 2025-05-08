package com.hepl.budgie.entity.attendancemanagement;

import com.hepl.budgie.config.auditing.AuditInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper=true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DayTypeDetail extends AuditInfo{

    private String dayTypeId;
    private String dayType;
    private String shiftCode;
}
