package com.hepl.budgie.repository.master;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.dto.menu.MenuStatus;
import com.hepl.budgie.dto.role.AuthorizationObj;
import com.hepl.budgie.dto.role.RoleBaseFieldsDTO;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.master.MasterForm;
import com.hepl.budgie.entity.role.Roles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface RolesRepository extends MongoRepository<Roles, String> {

        public static final String COLLECTION_NAME = "m_roles";

        default void updateSubMenuStatus(MongoTemplate mongoTemplate, MenuStatus menuStatus, String id, String menuName,
                        String groupCode) {
                Query query = new Query(new Criteria().andOperator(Criteria.where("_id").is(id),
                                Criteria.where("permissions.menu.name").is(menuName),
                                Criteria.where("permissions.subMenuPermissions.submenu.name")
                                                .is(menuStatus.getName())));
                Update update = new Update();
                update.set("permissions.$.subMenuPermissions.$[s].submenu.status", menuStatus.getStatus());
                update.filterArray(Criteria.where("s.submenu.name").is(menuStatus.getName()));

                mongoTemplate.updateFirst(query, update, getCollectionName(groupCode));
        }

        default void updateMenuStatus(MongoTemplate mongoTemplate, MenuStatus menuStatus, String id, String groupCode) {
                Query query = new Query(new Criteria().andOperator(Criteria.where("_id").is(id),
                                Criteria.where("permissions.menu.name").is(menuStatus.getName())));
                Update update = new Update();
                update.set("permissions.$.menu.status", menuStatus.getStatus());

                mongoTemplate.updateFirst(query, update, getCollectionName(groupCode));
        }

        default AggregationResults<RoleBaseFieldsDTO> fetchAllRoles(String grpCode, MongoTemplate mongoTemplate) {
                ProjectionOperation projectionOperation = Aggregation.project("roleName", "roleDescription", "status");

                Aggregation aggregation = Aggregation.newAggregation(projectionOperation);
                return mongoTemplate.aggregate(aggregation, getCollectionName(grpCode),
                                RoleBaseFieldsDTO.class);
        }

        default Optional<Roles> fetchByRoleIdAndGrpCode(String roleId, String grpCode, MongoTemplate mongoTemplate) {
                Query query = new Query(Criteria.where("_id").is(roleId));

                return Optional.ofNullable(mongoTemplate.findOne(query, Roles.class, getCollectionName(grpCode)));
        }

        default List<Roles> fetchAllRolesAndMenus(String grpCode, MongoTemplate mongoTemplate) {
                return mongoTemplate.findAll(Roles.class, getCollectionName(grpCode));
        }

        default long checkRoleAccess(AuthorizationObj auth,
                        MongoTemplate mongoTemplate) {

                Query query = new Query(new Criteria().andOperator(Criteria.where("roleName").is(auth.getRole()),
                                Criteria.where("permissions.menu.name").is(auth.getMenu()),
                                Criteria.where("permissions.subMenuPermissions.submenu.name").is(auth.getSubmenu()),
                                Criteria.where("permissions.subMenuPermissions.permissions").is(auth.getPermission())));
                return mongoTemplate.count(query, getCollectionName(auth.getGroupId()));
        }

        default void saveRole(Roles role, String groupCode, MongoTemplate mongoTemplate) {
                mongoTemplate.save(role, getCollectionName(groupCode));
        }

        default void updateRole(Roles role, String id, String groupCode, MongoTemplate mongoTemplate, UserRef userRef) {
                Query query = new Query(Criteria.where("_id").is(id));

                Update update = new Update();
                update.set("permissions", role.getPermissions());
                update.set("modifiedByUser", userRef.getEmpId());
                update.set("lastModifiedDate", LocalDateTime.now());

                mongoTemplate.updateFirst(query, update, getCollectionName(groupCode));
        }

        default Optional<Roles> findByNameAndGrp(String roleName, String groupCode, MongoTemplate mongoTemplate) {
                Query query = new Query(Criteria.where("roleName").is(roleName));

                return Optional.ofNullable(mongoTemplate.findOne(query, Roles.class, getCollectionName(groupCode)));
        }

        default AggregationResults<OptionsResponseDTO> getOptions(MongoTemplate mongoTemplate, String groupCode) {

                ProjectionOperation projectionOperation = Aggregation.project().and("roleName").as("value")
                                .and("roleName").as("name");

                Aggregation aggregation = Aggregation.newAggregation(projectionOperation);
                return mongoTemplate.aggregate(aggregation, getCollectionName(groupCode),
                                OptionsResponseDTO.class);
        }

        List<Roles> findByRoleNameIn(String accessTypes);

        // default void initMasterSettings(String groupCode, MongoTemplate mongoTemplate) {
        //         mongoTemplate.indexOps(getCollectionName(groupCode)).ensureIndex(
        //                         new Index("roleName", Sort.Direction.ASC).unique()
        //                                         .collation(Collation.of(Locale.US).strength(2)));
        // }

        default String getCollectionName(String groupCode) {
                return groupCode.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + groupCode);
        }

        
        default void initMasterRoles(String groupCode, MongoTemplate mongoTemplate) {
                mongoTemplate.indexOps(getCollectionName(groupCode)).ensureIndex(
                                new Index("roleName", Sort.Direction.ASC).unique().collation(Collation.of(Locale.US).strength(2)));

                Aggregation aggregation = Aggregation.newAggregation(Aggregation.out(getCollectionName(groupCode)));

                mongoTemplate.aggregate(aggregation, COLLECTION_NAME, Roles.class);
        }
}
