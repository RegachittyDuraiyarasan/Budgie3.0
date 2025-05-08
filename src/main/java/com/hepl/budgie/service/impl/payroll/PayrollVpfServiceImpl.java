package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollVpfDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollVpf;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.payroll.PayrollVpfRepository;
import com.hepl.budgie.service.payroll.PayrollVpfService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollVpfServiceImpl implements PayrollVpfService {

    private final PayrollVpfRepository payrollVpfRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;


    public void upsert(PayrollVpfDTO payrollVpfDTO, String operation) {

        if (operation.equalsIgnoreCase(DataOperations.SAVE.label)) {
            payrollVpfDTO.setRcpfId(payrollVpfRepository.findTopByOrderByIdDesc(getOrgCode(), mongoTemplate)
                    .map(e -> AppUtils.generateUniqueId(e.getRcpfId()))
                    .orElse("RCPF000001"));
            payrollVpfDTO.setStatus(Status.ACTIVE.label);
        } else if (operation.equalsIgnoreCase(DataOperations.UPDATE.label)) {
            String status = payrollVpfRepository.findByRcpfId(mongoTemplate, getOrgCode(), payrollVpfDTO.getRcpfId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND)).getStatus();
            payrollVpfDTO.setStatus(status);
        }
        payrollVpfDTO.setType("VPF");
        boolean existStatus = payrollVpfRepository.existsByEmpIdAndType(mongoTemplate, payrollVpfDTO, getOrgCode(), operation);

        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.SCHEME_ALREADY_EXISTS);

        log.info("VPF Request - {}", payrollVpfDTO);
        payrollVpfRepository.upsert(payrollVpfDTO, mongoTemplate, getOrgCode());


    }


    @Override
    public List<PayrollVpf> getAllData() {
        return payrollVpfRepository.findByTypeAndStatus(mongoTemplate, getOrgCode(), "VPF");
    }

    public String updateStatus(String id, String operation) {
        PayrollVpf payrollVpf = payrollVpfRepository.findByRcpfId(mongoTemplate, getOrgCode(), id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        String status = determineStatus(operation, payrollVpf.getStatus());

        log.info("Updated status for VPF: {} ", status);
        payrollVpfRepository.updateStatus(id, mongoTemplate, getOrgCode(), status);
        return status;
    }

    private String determineStatus(String operation, String currentStatus) {
        return switch (operation.toLowerCase()) {
            case "update" -> currentStatus.equalsIgnoreCase(Status.ACTIVE.label) ?
                    Status.INACTIVE.label : Status.ACTIVE.label;
            case "delete" -> Status.DELETED.label;
            default -> Status.ACTIVE.label;
        };
    }
    private String getOrgCode() {
       return jwtHelper.getOrganizationCode();
        // return "ORG00001";
    }



}
