package com.hepl.budgie.repository.payroll;

import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.payroll.PayrollComponentDTO;
import com.hepl.budgie.dto.payroll.PayrollGroupedComponentDTO;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.entity.payroll.payrollEnum.PayType;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface PayrollComponentRepository extends MongoRepository<PayrollComponent, String> {
    String COLLECTION_NAME = "payroll_m_component_";

    default List<PayrollComponent> fetchAllComponents(MongoTemplate mongoTemplate, String orgId) {
        Query query=new Query(Criteria.where("status").nin(Status.DELETED.label));
        return mongoTemplate.find(query, PayrollComponent.class, COLLECTION_NAME + orgId);
    }

    default Optional<PayrollComponent> findLatestComponent(String orgId, MongoTemplate mongoTemplate) {
        Query query = new Query().with(Sort.by(Sort.Direction.DESC, "id")).limit(1);
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollComponent.class, COLLECTION_NAME + orgId));
    }

    default boolean upsertComponent(PayrollComponentDTO dto, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("componentId").is(dto.getComponentId()));
        Update update = buildUpdateFromDTO(dto);
        UpdateResult result = mongoTemplate.upsert(query, update, PayrollComponent.class, COLLECTION_NAME + orgId);
        return result.wasAcknowledged();
    }

    default boolean existsByComponentNameAndStatus(String type, MongoTemplate mongoTemplate, PayrollComponentDTO dto, String orgId) {
        Query query = new Query();
            query.addCriteria(Criteria.where("componentName").is(dto.getComponentName()))
                .addCriteria(Criteria.where("status").nin(Status.DELETED.label));
        if (type.equalsIgnoreCase("update"))
            query.addCriteria(Criteria.where("componentId").nin(dto.getComponentId()));
        return mongoTemplate.exists(query, PayrollComponent.class, COLLECTION_NAME + orgId);
    }

    default void deleteComponent(String id, MongoTemplate mongoTemplate, String orgId) {
        updateField(id, "status", Status.DELETED.label, mongoTemplate, orgId);
    }

    default void updateComponentStatus(String id, String status, MongoTemplate mongoTemplate, String orgId) {
        updateField(id, "status", status, mongoTemplate, orgId);
    }

    default Optional<PayrollComponent> getByComponentId(String id, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("componentId").is(id));
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollComponent.class, COLLECTION_NAME + orgId));
    }

    default Optional<PayrollComponent> getByComponentName(String name, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("componentName").is(name).and("status").is(Status.ACTIVE.label));
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollComponent.class, COLLECTION_NAME + orgId));
    }

    default void updateField(String id, String fieldName, Object value, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("componentId").is(id));
        Update update = new Update().set(fieldName, value);
        mongoTemplate.findAndModify(query, update, PayrollComponent.class, COLLECTION_NAME + orgId);
    }

    default List<PayrollComponent> getFixedComponents(MongoTemplate mongoTemplate, String orgId, List<String> componentType){
        Query query = new Query(
                Criteria.where("componentType").in(componentType)
                        .and("status").in(Status.ACTIVE.label)
                        .and("payType").in(PayType.FIXED_PAY.label))
                .with(Sort.by(Sort.Direction.DESC, "componentType"));
        return mongoTemplate.find(query, PayrollComponent.class, COLLECTION_NAME + orgId);
    }
    default <T> List<T> getActiveCompByComponentTypeAndPayType(MongoTemplate mongoTemplate, List<String> componentType, String payType, Class<T> projectionType, String orgId){
        Query query = new Query(
                Criteria.where("componentType").in(componentType)
                        .and("status").in(Status.ACTIVE.label)
                        .and("payType").in(payType))
                .with(Sort.by(Sort.Direction.DESC, "componentType"));
        return mongoTemplate.find(query, projectionType, COLLECTION_NAME + orgId);
    }
    default <T> List<T> getArrearsComponents(MongoTemplate mongoTemplate, Class<T> projectionType, String orgId){
        Query query = new Query(
                Criteria.where("componentType").in(ComponentType.EARNINGS.label)
                        .and("status").in(Status.ACTIVE.label)
                        .and("payType").in(PayType.FIXED_PAY.label)
                        .and("arrearsCalc").is(true)
                )
                .with(Sort.by(Sort.Direction.DESC, "componentType"));
        return mongoTemplate.find(query, projectionType, COLLECTION_NAME + orgId);
    }
    default <T> List<T> getActiveCompByComponentType(MongoTemplate mongoTemplate, Class<T> projectionType, String orgId){
        Query query = new Query(Criteria.where("status").in(Status.ACTIVE.label))
                .with(Sort.by(Sort.Direction.DESC, "componentType"));
        return mongoTemplate.find(query, projectionType, COLLECTION_NAME + orgId);
    }
    default List<String> getActiveComponentsByOrgIdForExcel(MongoTemplate mongoTemplate, String orgId){
        Query query = new Query(Criteria.where("status").in(Status.ACTIVE.label))
                .with(Sort.by(Sort.Direction.DESC, "componentType"));
        List<String> activeComponents = new ArrayList<>();
        mongoTemplate.find(query, PayrollPayTypeCompDTO.class, COLLECTION_NAME + orgId)
                .forEach(comp -> {
                    activeComponents.add("Sup_" + comp.getComponentName().replaceAll("\\s+", "_"));
                    if (!comp.getComponentName().equalsIgnoreCase(ComponentType.REIMBURSEMENT.label)) {
                        activeComponents.add("Sup_" + comp.getComponentName().replaceAll("\\s+", "_") + "_Arrear");
                    }
                });
        return activeComponents;
    }

    default List<String> getVariableComponentsForExcel(MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(
                Criteria.where("componentType").in(List.of(ComponentType.EARNINGS.label, ComponentType.DEDUCTION.label))
                        .and("status").in(Status.ACTIVE.label)
                        .and("payType").in(PayType.VARIABLE_PAY.label))
                .with(Sort.by(Sort.Direction.DESC, "componentType"));
        return mongoTemplate.find(query, PayrollPayTypeCompDTO.class, COLLECTION_NAME + orgId)
                .stream()
                .map(comp -> comp.getComponentName().replaceAll("\\s+", "_") + "_(" + comp.getComponentType() + ")")
                .toList();

    }


    static Update buildUpdateFromDTO(PayrollComponentDTO dto) {
        return new Update()
                .set("componentId", dto.getComponentId())
                .set("componentName", dto.getComponentName())
                .set("componentSlug", dto.getComponentSlug())
                .set("componentType", dto.getComponentType())
                .set("payType", dto.getPayType())
                .set("compNamePaySlip", dto.getCompNamePaySlip())
                .set("proDataBasisCalc", dto.getProDataBasisCalc())
                .set("arrearsCalc", dto.getArrearsCalc())
                .set("compShowInPaySlip", dto.getCompShowInPaySlip())
                .set("status", dto.getStatus())
                .set("_class", PayrollComponent.class.getName());
    }


    default Optional<PayrollComponent> findByComponentTypeAndComponentName(PayrollComponentDTO request, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(
                Criteria.where("componentType").is(request.getComponentType())
                        .and("componentName").is(request.getComponentName())
        );
        return Optional.ofNullable(mongoTemplate.findOne(query, PayrollComponent.class, COLLECTION_NAME + orgId));
    }


    default boolean updateComponent(PayrollComponent component, Update update, MongoTemplate mongoTemplate, String orgId) {
        Query query = new Query(Criteria.where("componentId").is(component.getComponentId()));

        UpdateResult result = mongoTemplate.updateFirst(query, update, PayrollComponent.class, COLLECTION_NAME + orgId);
        return result.getModifiedCount() > 0;
    }

    default List<PayrollGroupedComponentDTO> componentType(MongoTemplate mongoTemplate, String orgId) {
        GroupOperation groupByComponentType = Aggregation.group("componentType")
                .push(Aggregation.ROOT).as("components");

        Aggregation aggregation = Aggregation.newAggregation(groupByComponentType,
                Aggregation.match(Criteria.where("components.status").is(Status.ACTIVE.label))
        );

        AggregationResults<PayrollGroupedComponentDTO> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME + orgId, PayrollGroupedComponentDTO.class);
        return results.getMappedResults();

    }
}
