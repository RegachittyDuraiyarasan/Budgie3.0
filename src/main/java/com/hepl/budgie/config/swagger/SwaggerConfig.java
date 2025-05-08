package com.hepl.budgie.config.swagger;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

    @Value("${com.custom.swagger-path}")
    private String path;

    @Value("${com.custom.swagger-live-path}")
    private String livePath;

    SecurityScheme securityScheme() {
        return new SecurityScheme().bearerFormat("JWT").type(Type.HTTP).scheme("bearer").name("Bearer Authentication");
    }

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Budgie 3.0")
                .description("Budgie 3.0 API Documentation")
                .version("v3.0.0")
                .contact(new Contact().email("prem.l@hepl.com")))
                .servers(List.of(new Server().url(livePath), new Server().url(path)))
                .schemaRequirement("Bearer Authentication", securityScheme());
    }
}
