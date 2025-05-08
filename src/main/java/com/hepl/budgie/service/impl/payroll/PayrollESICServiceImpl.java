package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollESICDto;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollESIC;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.payroll.PayrollESICRepository;
import com.hepl.budgie.service.payroll.PayrollESICService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@Slf4j
public class PayrollESICServiceImpl implements PayrollESICService {
    private final MongoTemplate mongoTemplate;
    private final PayrollESICRepository payrollESICRepository;
    private final JWTHelper jwtHelper;
    private final  static List<String> ORG_LIST = List.of("ORG001");

    public PayrollESICServiceImpl(MongoTemplate mongoTemplate, PayrollESICRepository payrollESICRepository, JWTHelper jwtHelper) {
        this.mongoTemplate = mongoTemplate;
        this.payrollESICRepository = payrollESICRepository;
        this.jwtHelper = jwtHelper;

    }

    public boolean upsert(PayrollESICDto payrollESICDto, String operation) {

        if (operation.equalsIgnoreCase(DataOperations.SAVE.label)) {

            payrollESICDto.setEsicId(payrollESICRepository.findTopByOrderByIdDesc("IN", mongoTemplate)
                    .map(e -> AppUtils.generateUniqueId(e.getEsicId()))
                    .orElse("ESI00001"));
            payrollESICDto.setStatus(Status.ACTIVE.label);

        } else if (operation.equalsIgnoreCase(DataOperations.UPDATE.label)) {

            String status = payrollESICRepository.findByEsicId(mongoTemplate, "IN", payrollESICDto.getEsicId(), getOrgCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,AppMessages.ID_NOT_FOUND)).getStatus();
            payrollESICDto.setStatus(status);
        }
        payrollESICDto.setOrgId(getOrgCode());

        boolean existStatus = payrollESICRepository.existsByOrgIdIn(mongoTemplate, payrollESICDto, "IN", operation);

        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT,AppMessages.STATE_ALREADY_EXISTS);
        log.info("ESIC Request - {}", payrollESICDto);
        return payrollESICRepository.upsert(payrollESICDto, mongoTemplate, "IN");
    }

    public List<PayrollESIC> list() {
        return payrollESICRepository.findByOrgId(mongoTemplate, "IN", getOrgCode());
    }

    public String updateStatus(String id, String operation) {

        PayrollESIC payrollESIC = payrollESICRepository.findByEsicId(mongoTemplate, "IN", id, getOrgCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));

        String status = determineStatus(operation,payrollESIC.getStatus());

        if(status.equalsIgnoreCase(Status.ACTIVE.label)){
            boolean existStatus = payrollESICRepository.existsByOrgIdInAndStatus(mongoTemplate, "IN", id, getOrgCode());
            if (existStatus)
                throw new ResponseStatusException(HttpStatus.CONFLICT,AppMessages.ESIC_ACTIVE_CONFLICT);
        }

        log.info("Updated status for ESIC: {} ", status);
        payrollESICRepository.updateStatus(id, mongoTemplate, "IN", status);

        return status;
    }

    private String determineStatus(String operation, String currentStatus) {
        return switch (operation.toLowerCase()) {
            case "update" -> currentStatus.equalsIgnoreCase(Status.ACTIVE.label) ?
                    Status.INACTIVE.label : Status.ACTIVE.label;
            case "delete" -> Status.DELETED.label;
            default -> Status.INACTIVE.label;
        };
    }

    private String getOrgCode(){
        return jwtHelper.getOrganizationCode();
//        return  "ORG00001";
    }

}
