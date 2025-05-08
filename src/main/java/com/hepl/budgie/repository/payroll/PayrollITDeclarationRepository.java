package com.hepl.budgie.repository.payroll;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.dto.payroll.HraDTO;
import com.hepl.budgie.dto.payroll.ItLetOutDTO;
import com.hepl.budgie.dto.payroll.LetOutDTO;
import com.hepl.budgie.dto.payroll.PayrollRelease;
import com.hepl.budgie.dto.payroll.PayrollRequestDTO;
import com.hepl.budgie.dto.payroll.PreviousEmploymentTaxDTO;
import com.hepl.budgie.dto.payroll.SchemeUpdateDTO;
import com.hepl.budgie.dto.payroll.SchemesDTO;
import com.hepl.budgie.entity.payroll.FamilyList;
import com.hepl.budgie.entity.payroll.PayrollHra;
import com.hepl.budgie.entity.payroll.PayrollITDeclaration;
import com.hepl.budgie.entity.payroll.PayrollITScheme;
import com.hepl.budgie.entity.payroll.PayrollLetOut;
import com.hepl.budgie.entity.payroll.PayrollLetOutProperties;
import com.hepl.budgie.entity.payroll.SchemeList;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.utils.AppMessages;

@Repository
public interface PayrollITDeclarationRepository extends MongoRepository<PayrollITDeclaration, String> {

    public static final String COLLECTION_NAME = "payroll_it_declaration";

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default List<PayrollITDeclaration> saveItDeclarationRelease(PayrollRequestDTO release,
            String orgId, String finYear, String planId, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        List<PayrollITDeclaration> declarations = new ArrayList<>();

        for (PayrollRelease rel : release.getPayrollRelease()) {
            PayrollITDeclaration declaration = new PayrollITDeclaration();
            declaration.setEmpId(rel.getEmpId());
            if (rel.getEndDate() != null) {
                ZonedDateTime zdt = rel.getEndDate().atStartOfDay(ZoneId.systemDefault());
                declaration.setEndDate(zdt);
            }
            declaration.setPlanId(planId);
            declaration.setStatus(DataOperations.CREATED.label);
            declaration.setFinancialYear(finYear);
            mongoTemplate.insert(declaration, collection);
            declarations.add(declaration);
        }
        return declarations;
    }

    default List<PayrollITDeclaration> findByStatus(String orgId, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(
                Criteria.where("status").nin(Arrays.asList(DataOperations.CREATED.label, DataOperations.DRAFT.label)));
        return mongoTemplate.find(query, PayrollITDeclaration.class, collection);
    }

    default List<PayrollITDeclaration> saveItDeclarationReRelease(PayrollRequestDTO release, String orgId,
            String finYear, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        List<PayrollITDeclaration> declarations = new ArrayList<>();

        for (PayrollRelease rel : release.getPayrollRelease()) {
            PayrollITDeclaration declaration = new PayrollITDeclaration();
            declaration.setEmpId(rel.getEmpId());
            if (rel.getEndDate() != null) {
                ZonedDateTime zdt = rel.getEndDate().atStartOfDay(ZoneId.systemDefault());
                declaration.setEndDate(zdt);
            }
            declaration.setStatus(DataOperations.DRAFT.label);
            declaration.setFinancialYear(finYear);
            mongoTemplate.insert(declaration, collection);
            declarations.add(declaration);
        }
        return declarations;
    }

    default Set<String> findExistingEmpIds(List<String> empIds, String orgId, MongoTemplate mongoTemplate) {
        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("empId").in(empIds));
        query.fields().include("empId");

        List<PayrollITDeclaration> declarations = mongoTemplate.find(query, PayrollITDeclaration.class, collection);

        return declarations.stream()
                .map(PayrollITDeclaration::getEmpId)
                .collect(Collectors.toSet());
    }

    default PayrollITDeclaration findByEmpId(String empId, String orgId, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("empId").is(empId));
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
    }

    default PayrollITDeclaration updateRegime(String orgId, String empId, String regime, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("empId").is(empId));
        Update update = new Update();
        update.set("regime", regime);
        return mongoTemplate.findAndModify(query, update, FindAndModifyOptions.options().returnNew(true),
                PayrollITDeclaration.class, collection);
    }

    default PayrollITDeclaration findLastPlanId(String orgId, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        return mongoTemplate.findOne(
                new Query().with(Sort.by(Sort.Direction.DESC, "_id")),
                PayrollITDeclaration.class,
                collection);

    }

    default PayrollITDeclaration updateSchemes(SchemeUpdateDTO schemes, PayrollITScheme itSchemes, String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        String type = schemes.getType();
        String planId = schemes.getPlanId();

        Query query = new Query(Criteria.where("planId").is(planId));
        PayrollITDeclaration declaration = mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
        if (declaration == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Declaration not found");
        }
        List<SchemeList> currentList = getSchemeListByType(declaration, type);
        List<SchemeList> updatedList = new ArrayList<>(currentList == null ? new ArrayList<>() : currentList);
        for (SchemesDTO schemeDto : schemes.getSchemes()) {
            boolean exists = updatedList.stream()
                    .anyMatch(s -> s.getTitle().equalsIgnoreCase(schemeDto.getTitle()));
            if (!exists) {
                SchemeList newScheme = new SchemeList();
                newScheme.setTitle(schemeDto.getTitle());
                newScheme.setDeclaredAmount(schemeDto.getAmount());
                newScheme.setSchemeId(schemeDto.getSchemeId());
                updatedList.add(newScheme);
            }
        }
        Update update = new Update();
        switch (type) {
            case "section80":
                update.set("section80", updatedList);
                break;
            case "chapter6":
                update.set("chapter6", updatedList);
                break;
            case "medical":
                update.set("medical", updatedList);
                break;
            case "otherIncome":
                update.set("otherIncome", updatedList);
                break;
            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_SCHEME_NOT_FOUND);
        }
        mongoTemplate.updateFirst(query, update, PayrollITDeclaration.class, collection);
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
    }

    default List<SchemeList> getSchemeListByType(PayrollITDeclaration declaration, String type) {
        return switch (type) {
            case "section80" -> declaration.getSection80();
            case "chapter6" -> declaration.getChapter6();
            case "medical" -> declaration.getMedical();
            case "otherIncome" -> declaration.getOtherIncome();
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_SCHEME_NOT_FOUND);
        };
    }

    default PayrollITDeclaration updateHra(String planId, HraDTO hraDtos, String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("planId").is(planId));

        List<PayrollHra> hraList = hraDtos.getHraDetails().stream().map(dto -> {
            if (dto.getTo() != null && dto.getFrom() != null && dto.getTo().isBefore(dto.getFrom())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_HRA_DATE_ERROR);
            }

            PayrollHra hra = new PayrollHra();
            LocalDate from = dto.getFrom();
            LocalDate to = dto.getTo();
            long monthsBetween = ChronoUnit.MONTHS.between(YearMonth.from(from), YearMonth.from(to)) + 1;

            hra.setFrom(from.atStartOfDay(ZoneId.systemDefault()));
            hra.setTo(to.atStartOfDay(ZoneId.systemDefault()));
            hra.setRent(dto.getRent());
            hra.setTotalRent(dto.getRent() * monthsBetween);
            hra.setLandlord(dto.getLandlord());
            hra.setHouseNumber(dto.getHouseNumber());
            hra.setStreet(dto.getStreet());
            hra.setCity(dto.getCity());
            hra.setPincode(dto.getPincode());
            hra.setLandlordName(dto.getLandlordName());
            hra.setLandlordPan(dto.getLandlordPan());
            hra.setLandlordHouseNumber(dto.getLandlordHouseNumber());
            hra.setLandlordStreet(dto.getLandlordStreet());
            hra.setLandlordCity(dto.getLandlordCity());
            hra.setLandlordPincode(dto.getLandlordPincode());
            return hra;
        }).collect(Collectors.toList());

        Update update = new Update().set("hra", hraList);
        mongoTemplate.updateFirst(query, update, PayrollITDeclaration.class, collection);
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
    }

    default PayrollITDeclaration updateLetOut(String planId, LetOutDTO letOutDTO, String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("planId").is(planId));
        PayrollITDeclaration declaration = mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
        if (declaration == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Declaration not found");
        }

        ItLetOutDTO itLetOut = letOutDTO.getItLetOut();
        PayrollLetOut letOut = new PayrollLetOut();
        letOut.setName(itLetOut.getName());
        letOut.setDeclaredAmount(itLetOut.getDeclaredAmount());
        letOut.setPan(itLetOut.getPan());
        letOut.setDateofAvailing(
                itLetOut.getDateofAvailing() != null ? itLetOut.getDateofAvailing().atStartOfDay(ZoneId.systemDefault())
                        : null);
        letOut.setDateOfAcquisition(itLetOut.getDateOfAcquisition() != null
                ? itLetOut.getDateOfAcquisition().atStartOfDay(ZoneId.systemDefault())
                : null);
        List<PayrollLetOutProperties> letOutProps = letOutDTO.getItLetOutProperties().stream().map(dto -> {
            PayrollLetOutProperties prop = new PayrollLetOutProperties();
            prop.setLetableAmount(dto.getLetableAmount());
            prop.setMunicipalTax(dto.getMunicipalTax());
            prop.setUnrealizedTax(dto.getUnrealizedTax());
            prop.setLoanInterest(dto.getLoanInterest());
            prop.setLenderName(dto.getLenderName());
            prop.setLenderPan(dto.getLenderPan());
            prop.setAvailableDate(
                    dto.getAvailableDate() != null ? dto.getAvailableDate().atStartOfDay(ZoneId.systemDefault())
                            : null);
            prop.setDateOfAcquisition(
                    dto.getDateOfAcquisition() != null ? dto.getDateOfAcquisition().atStartOfDay(ZoneId.systemDefault())
                            : null);
            long netValue = dto.getLetableAmount() - dto.getMunicipalTax() - dto.getUnrealizedTax();
            long stdDeduction = Math.round(netValue * 0.30);
            long incomeOrLoss = stdDeduction - dto.getLoanInterest();

            prop.setNetValue(netValue);
            prop.setStandardDeduction(stdDeduction);
            prop.setIncomeOrLoss(incomeOrLoss);

            return prop;
        }).collect(Collectors.toList());

        Update update = new Update()
                .set("itLetOut", letOut)
                .set("itLetOutProperties", letOutProps);

        mongoTemplate.updateFirst(query, update, PayrollITDeclaration.class, collection);
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
    }

    default PayrollITDeclaration updateMetro(String metro, String planId, String empId, String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("empId").is(empId).and("planId").is(planId));
        Update update = new Update().set("metro", metro);
        mongoTemplate.updateFirst(query, update, PayrollITDeclaration.class, collection);
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
    }

    default PayrollITDeclaration updatePreviousEmployee(String planId, PreviousEmploymentTaxDTO employeeTax,
            String orgId, MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("planId").is(planId));
        Update update = new Update();
        update.set("previousEmployeeTax", employeeTax);
        update.inc("consdider", 1);
        update.set("status", Status.COMPLETED.label);
        mongoTemplate.updateFirst(query, update, PayrollITDeclaration.class, collection);
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
    }

    default PayrollITDeclaration updateDraftPreviousEmployee(String planId, PreviousEmploymentTaxDTO employeeTax,
            String orgId,
            MongoTemplate mongoTemplate) {

        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("planId").is(planId));
        Update update = new Update();
        update.set("previousEmployeeTax", employeeTax);
        update.set("status", DataOperations.DRAFT.label);
        mongoTemplate.updateFirst(query, update, PayrollITDeclaration.class, collection);
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);

    }

    default PayrollITDeclaration updateFamilies(String planId, List<FamilyList> familyList, String orgId,
            MongoTemplate mongoTemplate) {
        String collection = getCollectionName(orgId);
        Query query = new Query(Criteria.where("planId").is(planId));
        Update update = new Update();
        update.set("family", familyList);
        mongoTemplate.updateFirst(query, update, PayrollITDeclaration.class, collection);
        return mongoTemplate.findOne(query, PayrollITDeclaration.class, collection);
    }

}
