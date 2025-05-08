package com.hepl.budgie.config.db;

import java.util.Locale;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.stereotype.Component;

import com.hepl.budgie.entity.menu.Menu;
import com.hepl.budgie.entity.organization.Organization;

import lombok.extern.slf4j.Slf4j;

@Component
@DependsOn("mongoTemplate") // mongoTemplate if single datasource
@Slf4j
public class Indexing {

        private final MongoTemplate mongoTemplate;

        public Indexing(MongoTemplate mongoTemplate) {
                this.mongoTemplate = mongoTemplate;
        }

        @EventListener(ApplicationReadyEvent.class)
        void ensureIndexes() {

                // Collation for checking case-insensitive data
                mongoTemplate.indexOps(Organization.class).ensureIndex(
                                new Index("organizationDetail", Sort.Direction.ASC).unique()
                                                .collation(Collation.of(Locale.US).strength(2)));

                mongoTemplate.indexOps(Organization.class).ensureIndex(
                                new Index("organizationCode", Sort.Direction.ASC).unique());

                mongoTemplate.indexOps(Menu.class).ensureIndex(
                                new Index("name", Sort.Direction.ASC).unique()
                                                .collation(Collation.of(Locale.US).strength(2)));

        }

}
