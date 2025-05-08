package com.hepl.budgie.service.impl.payroll;


import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollTdsDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.countriesdetails.Country;
import com.hepl.budgie.entity.payroll.PayrollTds;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.payroll.PayrollTdsRepository;
import com.hepl.budgie.service.payroll.PayrollTdsService;
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
public class PayrollTdsServiceImpl implements PayrollTdsService {
    private final PayrollTdsRepository payrollTdsRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private static final String COUNTRY = "IN";

    public boolean upsert(PayrollTdsDTO payrollTdsDTO, String operation) {

        if (operation.equalsIgnoreCase(DataOperations.SAVE.label)) {

            payrollTdsDTO.setTdsSlabId(payrollTdsRepository.findTopByOrderByIdDesc(COUNTRY, mongoTemplate)
                    .map(e -> AppUtils.generateUniqueId(e.getTdsSlabId()))
                    .orElse("TDS000001"));
            payrollTdsDTO.setStatus(Status.ACTIVE.label);

        } else if (operation.equalsIgnoreCase(DataOperations.UPDATE.label)) {

            String status = payrollTdsRepository.findByTdsSlabId(mongoTemplate, COUNTRY, payrollTdsDTO.getTdsSlabId(), getOrgCode())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND)).getStatus();

            payrollTdsDTO.setStatus(status);
        }
        payrollTdsDTO.setOrgId(getOrgCode());
        boolean existStatus = payrollTdsRepository.existsByTypeAndOrgIdIn(mongoTemplate, payrollTdsDTO, COUNTRY, operation);

        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.SLAB_ALREADY_EXISTS);

        log.info("TDS Request - {}", payrollTdsDTO);
        return payrollTdsRepository.upsert(payrollTdsDTO, mongoTemplate, COUNTRY);
    }

    public List<PayrollTds> list() {

        return payrollTdsRepository.findByOrgId(mongoTemplate, COUNTRY, getOrgCode());

    }

    public void delete(String id) {
        payrollTdsRepository.findByTdsSlabId(mongoTemplate, COUNTRY, id, getOrgCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        payrollTdsRepository.deleteTdsSlab(id, mongoTemplate, COUNTRY);
    }

    public String updateStatus(String id) {
        PayrollTds payrollTds = payrollTdsRepository.findByTdsSlabId(mongoTemplate, COUNTRY, id, getOrgCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        String status = payrollTds.getStatus().equalsIgnoreCase(Status.ACTIVE.label) ?
                Status.INACTIVE.label : Status.ACTIVE.label;
        payrollTdsRepository.updateStatus(id, mongoTemplate, COUNTRY, status);
        return status;
    }

    private String getOrgCode() {
//        return "ORG00001";
        return jwtHelper.getOrganizationCode();
    }
}
