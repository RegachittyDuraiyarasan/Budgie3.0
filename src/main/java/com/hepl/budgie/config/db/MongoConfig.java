package com.hepl.budgie.config.db;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.MongoConverterConfigurationAdapter;

import com.hepl.budgie.service.KMSHandlerService;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClient;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.internal.build.MongoDriverVersion;

import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static java.lang.System.getProperty;

@Configuration
@EnableMongoRepositories(basePackages = "com.hepl.budgie.repository")
@RequiredArgsConstructor
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value(value = "${spring.data.mongodb.database}")
    private String database;
    @Value(value = "${spring.data.mongodb.uri}")
    private String dbConnection;

    private final KMSHandlerService kmsHandler;

    private MongoDriverInformation getMongoDriverInfo() {
        return MongoDriverInformation.builder()
                .driverName(MongoDriverVersion.NAME)
                .driverVersion(MongoDriverVersion.VERSION)
                .driverPlatform(format("Java/%s/%s", getProperty("java.vendor", "unknown-vendor"),
                        getProperty("java.runtime.version", "unknown-version")))
                .build();
    }

    private MongoClientSettings getMongoClientSettings() {

        return MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(dbConnection))
                .build();
    }

    @Override
    protected void configureConverters(MongoConverterConfigurationAdapter converterConfigurationAdapter) {
        converterConfigurationAdapter.registerConverter(new BinaryToBsonBinaryConverter());
        converterConfigurationAdapter.registerConverter(new BsonBinaryToBinaryConverter());
        converterConfigurationAdapter.registerConverter(new ZonedDateTimeReadConverter());
        converterConfigurationAdapter.registerConverter(new ZonedDateTimeWriteConverter());
    }

    @Override
    public MongoClient mongoClient() {
        kmsHandler.buildOrValidateVault();
        return new MongoClientImpl(getMongoClientSettings(), getMongoDriverInfo());
    }

    @Override
    protected String getDatabaseName() {
        return database;
    }

}
