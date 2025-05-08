package com.hepl.budgie.repository.organization;

import com.hepl.budgie.dto.organization.OrganizationMapAddDTO;
import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.OrganizationMap;

import com.mongodb.client.result.UpdateResult;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public interface OrganizationMapRepository extends MongoRepository<OrganizationMap, String> {

        public static final String COLLECTION_NAME = "organisation_map";

        Optional<OrganizationMap> findTopByOrderByIdDesc();

        Optional<OrganizationMap> findByParentOrganizationId(String parentId);

        Optional<OrganizationMapAddDTO> findByParentOrganization(String organizationName);

        default AggregationResults<MasterFormOptions> getParentOrganization(MongoTemplate mongoTemplate) {

                MatchOperation matchOperation = Aggregation.match(
                        Criteria.where("parentOrganization").ne(null)
                                .and("status").ne("Deleted")
                    );
                ProjectionOperation projectionOperation = Aggregation
                                .project()
                                .and("parentOrganization.organizationDetail")
                                .as("name")
                                .and("parentOrganization.organizationCode")
                                .as("value");
                Aggregation aggregation = Aggregation.newAggregation(matchOperation,projectionOperation);

                return mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
                                MasterFormOptions.class);
        }

        default AggregationResults<MasterFormOptions> getChildOrganization(String parentOrg,
                        MongoTemplate mongoTemplate) {

                MatchOperation matchOperation = Aggregation
                                .match(Criteria.where("parentOrganization.organizationCode").is(parentOrg).and("status").ne("Deleted"));
                UnwindOperation unwindOperation = Aggregation.unwind("organizationMapping");
                ProjectionOperation projectionOperation = Aggregation
                                .project()
                                .and("organizationMapping.organizationDetail")
                                .as("name")
                                .and("organizationMapping.organizationCode")
                                .as("value");
                Aggregation aggregation = Aggregation.newAggregation(matchOperation, unwindOperation,
                                projectionOperation);

                return mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
                                MasterFormOptions.class);
        }

        default Optional<OrganizationMap> checkIfOrganisationExists(List<String> organizationCodes,
                        MongoTemplate mongoTemplate) {

                Query query = new Query(
                                Criteria.where("organizationMapping.organizationCode").in(organizationCodes));

                return Optional.ofNullable(mongoTemplate.findOne(query, OrganizationMap.class));
        }

        default Optional<OrganizationMap> updateOrganizationMapStatus(String id, String status,
                        MongoTemplate mongoTemplate) {
                Query query = new Query(Criteria.where("_id").is(id));

                Update update = new Update();
                update.set("status", status);

                FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
                return Optional.ofNullable(mongoTemplate.findAndModify(query, update, options, OrganizationMap.class));
        }

        default Optional<OrganizationMap> updateOrganizationMap(String id, OrganizationMapAddDTO updateRequest,
                        MongoTemplate mongoTemplate) {
                // Fetch parent organization details
                Query parentQuery = new Query(
                                Criteria.where("organizationCode").is(updateRequest.getOrganizationDetail()));
                Organization parentOrganization = mongoTemplate.findOne(parentQuery, Organization.class);

                OrganizationRef parentOrganizationRef = (parentOrganization != null)
                                ? new OrganizationRef(parentOrganization.getOrganizationDetail(),
                                                parentOrganization.getOrganizationCode(),
                                                parentOrganization.getCountry(), parentOrganization.getIso3(),
                                                parentOrganization.getGroupId(), parentOrganization.getLogo(),
                                                List.of())
                                : null;

                // Fetch organizationMapping details
                List<OrganizationRef> organizationRefs = updateRequest.getOrganizationMapping().stream()
                                .map(orgCode -> {
                                        Query query = new Query(Criteria.where("organizationCode").is(orgCode));
                                        Organization organization = mongoTemplate.findOne(query, Organization.class);

                                        if (organization == null) {
                                                return null;
                                        }

                                        return new OrganizationRef(organization.getOrganizationDetail(),
                                                        organization.getOrganizationCode(), organization.getCountry(),
                                                        parentOrganization.getIso3(),
                                                        parentOrganization.getGroupId(),
                                                        parentOrganization.getLogo(), List.of());
                                })
                                .filter(Objects::nonNull)
                                .toList();

                // Create query to update organizationMap
                Query query = new Query(Criteria.where("id").is(id));
                Update update = new Update()
                                .set("parentOrganization", parentOrganizationRef)
                                .set("organizationMapping", organizationRefs);

                UpdateResult result = mongoTemplate.updateFirst(query, update, OrganizationMap.class);

                if (result.getMatchedCount() > 0) {
                        return Optional.ofNullable(mongoTemplate.findOne(query, OrganizationMap.class));
                } else {
                        return Optional.empty();
                }
        }

}
