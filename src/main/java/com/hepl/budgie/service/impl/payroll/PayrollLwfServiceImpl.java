package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollLwfDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollLwf;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.payroll.PayrollLwfRepository;
import com.hepl.budgie.service.payroll.PayrollLwfService;
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
public class PayrollLwfServiceImpl implements PayrollLwfService {
    private static final String COUNTRY = "IN";
    private final PayrollLwfRepository payrollLwfRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;


    public boolean upsert(PayrollLwfDTO payrollLwfDTO, String operation) {

        if (operation.equalsIgnoreCase(DataOperations.SAVE.label)) {

            payrollLwfDTO.setLwfId(payrollLwfRepository.findTopByOrderByIdDesc(COUNTRY, mongoTemplate)
                    .map(e -> AppUtils.generateUniqueId(e.getLwfId()))
                    .orElse("LWF00001"));

        } else if (operation.equalsIgnoreCase(DataOperations.UPDATE.label)) {

            payrollLwfRepository.findByLwfId(mongoTemplate, COUNTRY, payrollLwfDTO.getLwfId(), getOrgCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        }
        payrollLwfDTO.setStatus(Status.ACTIVE.label);
        payrollLwfDTO.setOrgId(getOrgCode());


        boolean existStatus = payrollLwfRepository.existsByStateAndOrgIdIn(mongoTemplate, payrollLwfDTO, COUNTRY, operation);

        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.STATE_ALREADY_EXISTS);

        payrollLwfDTO.setTotalContribution(payrollLwfDTO.getEmployeeContribution() + payrollLwfDTO.getEmployerContribution());
        log.info("LWF Request - {}", payrollLwfDTO);

        return payrollLwfRepository.upsert(payrollLwfDTO, mongoTemplate, COUNTRY);
    }

    public List<PayrollLwf> list() {

        return payrollLwfRepository.findByOrgId(mongoTemplate, COUNTRY, getOrgCode());

    }

    public void delete(String id) {

        payrollLwfRepository.findByLwfId(mongoTemplate, COUNTRY, id, getOrgCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));

        payrollLwfRepository.deleteLwf(id, mongoTemplate, COUNTRY);
    }

    private String getOrgCode(){
//        return "ORG00001";
        return jwtHelper.getOrganizationCode();
    }
}
