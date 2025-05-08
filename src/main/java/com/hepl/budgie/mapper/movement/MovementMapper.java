package com.hepl.budgie.mapper.movement;

import com.hepl.budgie.dto.movement.MovementFetchDTO;
import com.hepl.budgie.entity.movement.Movement;
import com.hepl.budgie.entity.movement.MovementDetails;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;


@Mapper(componentModel = "spring")
public interface MovementMapper {
    MovementMapper INSTANCE = Mappers.getMapper(MovementMapper.class);

    @Mappings({
            @Mapping(target = "movementId", source = "detail.movementId"),
            @Mapping(target = "empId", source = "movement.empId"),
            @Mapping(target = "movementInitializer", source = "detail.movementInitializer"),
            @Mapping(target = "movementType", source = "detail.movementType"),
            @Mapping(target = "movementTypeId", source = "detail.movementTypeId"),
            @Mapping(target = "movementTeamId", source = "detail.movementTeamId"),
            @Mapping(target = "departmentOld", source = "detail.department.old"),
            @Mapping(target = "departmentNow", source = "detail.department.now"),
            @Mapping(target = "designationOld", source = "detail.designation.old"),
            @Mapping(target = "designationNow", source = "detail.designation.now"),
            @Mapping(target = "supervisorOld", source = "detail.supervisor.old"),
            @Mapping(target = "supervisorNow", source = "detail.supervisor.now"),
            @Mapping(target = "reviewerOld", source = "detail.reviewer.old"),
            @Mapping(target = "reviewerNow", source = "detail.reviewer.now"),
            @Mapping(target = "supervisorRemarks", source = "detail.supervisorRemarks"),
            @Mapping(target = "assignedReviewerStatus", source = "detail.assignedReviewerStatus.status"),
            @Mapping(target = "officialReviewerStatus", source = "detail.officialReviewerStatus.status"),
            @Mapping(target = "hrStatus", source = "detail.hrStatus.status"),
            @Mapping(target = "isWithdrawn", source = "detail.isWithdrawn"),
            @Mapping(target = "effectiveFrom", source = "detail.effectiveFrom"),
            @Mapping(target = "createdAt", source = "detail.createdAt")
    })
    MovementFetchDTO toDto(MovementDetails detail, Movement movement);


}
