package com.hepl.budgie.repository.organization;

import com.hepl.budgie.dto.organization.OrganizationAddDTO;
import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.OrganizationMap;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends MongoRepository<Organization, String> {

    public static final String COLLECTION_NAME = "organisation";

    List<Organization> findByOrganizationCodeIn(List<String> organizationCodes);

    default Optional<Organization> findByOrganizationDetail(String organizationDetail, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("organizationDetail").is(organizationDetail));
        query.collation(Collation.of("en").strength(Collation.ComparisonLevel.secondary())); // Case-insensitive
        return Optional.ofNullable(mongoTemplate.findOne(query, Organization.class));
    }

    default void updateGroupIdForOrgs(List<String> orgCode, String groupId, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("organizationCode").in(orgCode));

        Update update = new Update();
        update.set("groupId", groupId);

        mongoTemplate.updateMulti(query, update, COLLECTION_NAME);
    }

    Optional<Organization> findTopByOrderByIdDesc();

    List<Organization> findByStatusNot(String status);

    default Optional<Organization> updateOrganization(String id, OrganizationAddDTO request,
            MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("_id").is(id));

        Update update = new Update();
        update.set("organizationDetail", request.getOrganizationDetail());
        update.set("email", request.getEmail());
        update.set("industryType", request.getIndustryType());
        update.set("tdsCircle", request.getTdsCircle());
        update.set("gstNumber", request.getGstNumber());
        update.set("contactNumber", request.getContactNumber());
        update.set("address", request.getAddress());
        update.set("state", request.getState());
        update.set("country", request.getCountry());
        update.set("sequence", request.getSequence());
        update.set("town", request.getTown());
        update.set("smtpPort", request.getSmtpPort());
        update.set("smtpProvider", request.getSmtpProvider());
        update.set("smtpServer", request.getSmtpServer());
        update.set("userName", request.getUserName());
        update.set("password", request.getPassword());
        update.set("fromMail", request.getFromMail());
        if (!request.getLogoFile().isEmpty() && !request.getLogoFile().isEmpty()) {
            update.set("logo", request.getLogo());
        }
        if (!request.getHeadSignature().isEmpty() && !request.getHeadSignature().isEmpty()) {
            update.set("signature", request.getSignature());
        }
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, Organization.class));
    }

    default UpdateResult deleteOrganization(String id, MongoTemplate mongoTemplate) {
        Query query = new Query(
                new Criteria().and("_id").is(id));

        Update update = new Update();
        update.set("status", Status.DELETED.label);

        return mongoTemplate.updateFirst(query, update, Organization.class);
    }

    boolean existsByOrganizationCode(String parentOrganization);

    default AggregationResults<OrganizationRef> getOrganisationReference(MongoTemplate mongoTemplate,
            List<String> organisationCodes) {

        MatchOperation matchOperation = Aggregation.match(Criteria.where("organizationCode").in(organisationCodes));
        ProjectionOperation projectionOperation = Aggregation
                .project("organizationDetail", "organizationCode", "country", "iso3", "groupId", "logo");

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectionOperation);

        return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, OrganizationRef.class);
    }

    default UpdateResult deleteOrganizationMap(String id, MongoTemplate mongoTemplate) {
        Query query = new Query(
                new Criteria().and("_id").is(id));

        Update update = new Update();
        update.set("status", Status.DELETED.label);

        return mongoTemplate.updateFirst(query, update, OrganizationMap.class);
    }

    Optional<Organization> findByOrganizationCode(String organizationCode);

    Optional<Organization> findByOrganizationDetail(String organizationDetail);

    default Optional<Organization> updateOrganizationStatus(String id, String status, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("_id").is(id));

        Update update = new Update();
        update.set("status", status);

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, Organization.class));
    }

    default List<String> getOrganizationCodes(MongoTemplate mongoTemplate) {
        ProjectionOperation projectStage = Aggregation.project("organizationCode");
        Aggregation aggregation = Aggregation.newAggregation(projectStage);
        AggregationResults<Organization> results = mongoTemplate.aggregate(aggregation, "organisation", Organization.class);

        return results.getMappedResults().stream()
                .map(Organization::getOrganizationCode)
                .toList();
    }

    default List<Organization> findByOrgIdAndIt(MongoTemplate mongoTemplate, String orgId){

        Query query = new Query(Criteria.where("organizationCode").is(orgId).and("sequence.itDeclaration").is(true));
        return mongoTemplate.find(query, Organization.class, COLLECTION_NAME);
    }

}
