package com.hepl.budgie.mapper.leavemanagement;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import com.hepl.budgie.dto.leave.CompOffDto;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.entity.leave.LeaveApplyDates;
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeaveMapper {
    

    @Mapping(target = "leaveType", constant = "Comp Off")
    @Mapping(target = "leaveCategory", constant = "Comp Off")
    @Mapping(target = "leaveCancel", constant = "No")
    @Mapping(target = "status", constant = "Pending")
    @Mapping(target = "fromDate", source = "comp.compOffDate")
    @Mapping(target = "toDate", source = "comp.compOffDate")
    @Mapping(target = "fromSession", source = "comp.fromSession.label")
    @Mapping(target = "toSession", source = "comp.toSession.label")
    @Mapping(target = "dateList", expression = "java(java.util.Collections.singletonList(comp.getCompOffDate()))")
    @Mapping(target = "contactNo", source = "contact")
    @Mapping(target = "numOfDays", expression = "java(comp.getFromSession().equals(comp.getToSession()) ? 0.5 : 1.0)")
    @Mapping(target = "workDate", expression = "java(comp.getDate() != null ? comp.getDate() : comp.getWorkedDate())")
    @Mapping(target = "leaveApply", source = "leaveDates")
    LeaveApply toLeaveApply(CompOffDto comp, String empId, String orgCode, String contact, List<LeaveApplyDates> leaveDates);

    @Mapping(target = "date", source = "comp.compOffDate")
    @Mapping(target = "fromSession", source = "comp.fromSession.label")
    @Mapping(target = "toSession", source = "comp.toSession.label")
    @Mapping(target = "leaveType", expression = "java(comp.getFromSession().equals(comp.getToSession()) ? \"Half Day\" : \"Full Day\")")
    @Mapping(target = "count", expression = "java(comp.getFromSession().equals(comp.getToSession()) ? 0.5 : 1.0)")
    @Mapping(target = "isHalfDay", expression = "java(comp.getFromSession().equals(comp.getToSession()))")
    @Mapping(target = "status", constant = "Pending")
    LeaveApplyDates toLeaveApplyDate(CompOffDto comp);
}
