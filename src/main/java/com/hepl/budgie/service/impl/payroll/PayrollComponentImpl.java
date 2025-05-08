package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollComponentDTO;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.repository.payroll.PayrollComponentRepository;
import com.hepl.budgie.service.payroll.PayrollComponentService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollComponentImpl implements PayrollComponentService {

    private final PayrollComponentRepository payrollComponentRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    /**
     * Helper method to generate a slug by joining words with underscores and converting to lowercase.
     */
    private String generateSlug(String componentName) {
        return String.join("_", componentName.split(" ")).toLowerCase();
    }

    @Override
    public List<PayrollPayTypeCompDTO> list(List<String> compType, String payType) {
        return payrollComponentRepository.getActiveCompByComponentTypeAndPayType(mongoTemplate, compType, payType, PayrollPayTypeCompDTO.class, jwtHelper.getOrganizationCode());
    }

    @Override
    public List<PayrollComponent> fetch() {
        return payrollComponentRepository.fetchAllComponents(mongoTemplate, jwtHelper.getOrganizationCode());
    }

    @Override
    public void upsert(String func, PayrollComponentDTO request) {
        request.setComponentSlug(generateSlug(request.getComponentName()));
        boolean result = payrollComponentRepository.upsertComponent(request, mongoTemplate, jwtHelper.getOrganizationCode());
        if(!result)
            throw new CustomResponseStatusException(AppMessages.COMPONENT_ID_NOT_FOUND , HttpStatus.BAD_REQUEST, new Object[]{func});
    }

    @Override
    public void upsertComponent(String func, PayrollComponentDTO request) {
        boolean check = false;
        if("save".equalsIgnoreCase(func)) {
            request.setComponentId(payrollComponentRepository
                .findLatestComponent(jwtHelper.getOrganizationCode(), mongoTemplate)
                .map(e-> AppUtils.generateUniqueId(e.getComponentId()))
                .orElse("COM00001"));
            check = payrollComponentRepository.existsByComponentNameAndStatus("save", mongoTemplate, request, jwtHelper.getOrganizationCode());
        }else if("update".equalsIgnoreCase(func)) {
            check = payrollComponentRepository.existsByComponentNameAndStatus("update",mongoTemplate, request, jwtHelper.getOrganizationCode());
        }
        if(check)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.COMPONENT_ID_ALREADY_FOUND);
        request.setComponentSlug(generateSlug(request.getComponentName()));
        boolean result = payrollComponentRepository.upsertComponent(request, mongoTemplate, jwtHelper.getOrganizationCode());
        if(!result)
            throw new CustomResponseStatusException(AppMessages.COMPONENT_ID_NOT_FOUND, HttpStatus.BAD_REQUEST, new Object[]{request.getComponentId()});
    }

    @Override
    public void update(PayrollComponentDTO request) {
        log.info("Component Info : {}", request);

        // Fetch existing record by componentId
        Optional<PayrollComponent> existingComponentOpt = payrollComponentRepository.getByComponentId(
                request.getComponentId(), mongoTemplate, "IN");

        if (existingComponentOpt.isPresent()) {
            PayrollComponent existingComponent = existingComponentOpt.get();

            // Check if request contains at least one existing orgId
//            boolean hasMatchingOrgId = existingComponent.getOrgId().stream()
//                    .anyMatch(request.getOrgId()::contains);
//
//            if (hasMatchingOrgId &&
//                    existingComponent.getComponentType().equals(request.getComponentType()) &&
//                    existingComponent.getComponentName().equals(request.getComponentName())) {
//                log.warn("Update not allowed: Matching componentType, componentName, and orgId found.");
//                throw new RuntimeException("Duplicate record found. Update not allowed.");
//            }

            // Track changes and update only modified fields
            boolean isUpdated = false;
            Update update = new Update();

            if (!existingComponent.getPayType().equals(request.getPayType())) {
                update.set("payType", request.getPayType());
                isUpdated = true;
            }
            if (!existingComponent.getCompNamePaySlip().equals(request.getCompNamePaySlip())) {
                update.set("compNamePaySlip", request.getCompNamePaySlip());
                isUpdated = true;
            }
            if (existingComponent.isProDataBasisCalc()) {
                update.set("proDataBasisCalc", request.getProDataBasisCalc());
                isUpdated = true;
            }
            if (!existingComponent.isProDataBasisCalc()) {
                update.set("arrearsCalc", request.getArrearsCalc());
                isUpdated = true;
            }
            if (!existingComponent.isCompShowInPaySlip()) {
                update.set("compShowInPaySlip", request.getCompShowInPaySlip());
                isUpdated = true;
            }

            // Merge orgId lists and update if new values exist
//            Set<String> updatedOrgIds = new HashSet<>(existingComponent.getOrgId());
//            updatedOrgIds.addAll(request.getOrgId());

//            if (!updatedOrgIds.equals(new HashSet<>(existingComponent.getOrgId()))) {
//                update.set("orgId", new ArrayList<>(updatedOrgIds));
//                isUpdated = true;
//            }
            log.info("Updated : {}", update);
            if (isUpdated) {
                boolean updated = payrollComponentRepository.updateComponent(existingComponent, update, mongoTemplate, "IN");
                log.info("Updated component successfully.");
            } else {
                log.info("No changes detected. No update performed.");
            }
        } else {
            log.warn("No matching record found with componentId: {}", request.getComponentId());
            throw new RuntimeException("Component not found.");
        }
    }

    @Override
    public void delete(String id) {
        payrollComponentRepository.deleteComponent(id, mongoTemplate, jwtHelper.getOrganizationCode());
    }

    @Override
    public String status(String id) {
        Optional<PayrollComponent> existingComponent = payrollComponentRepository.getByComponentId(id, mongoTemplate, jwtHelper.getOrganizationCode());
        if (existingComponent.isEmpty())
            throw new CustomResponseStatusException(AppMessages.COMPONENT_ID_NOT_FOUND, HttpStatus.BAD_REQUEST, new Object[]{id});

        String status = existingComponent.get().getStatus().equals(Status.ACTIVE.label) ? Status.INACTIVE.label : Status.ACTIVE.label;
        payrollComponentRepository.updateComponentStatus(id, status, mongoTemplate, jwtHelper.getOrganizationCode());
        return status;
    }

}
