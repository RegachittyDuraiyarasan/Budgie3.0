package com.hepl.budgie.config.swagger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SwaggerBlockTransform extends SwaggerIndexPageTransformer {

    public SwaggerBlockTransform(SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerWelcomeCommon swaggerWelcomeCommon, ObjectMapperProvider objectMapperProvider) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformerChain)
            throws IOException {
        log.info("Setting swagger transform");
        if (resource.toString().contains("index.html")) {
            final InputStream is = resource.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            try (BufferedReader br = new BufferedReader(isr)) {
                String html = br.lines().collect(Collectors.joining());
                html = html.replace("<title>Swagger UI</title>", "<title>Budgie</title>");
                html = html
                        .replace(
                                "<link rel=\"icon\" type=\"image/png\" href=\"./favicon-32x32.png\" sizes=\"32x32\" />",
                                "<link rel=\"icon\" type=\"image/png\" href=\"https://dev.budgie.co.in/budgie_3/favicon.ico\" sizes=\"32x32\" />");
                html = html
                        .replace(
                                "<link rel=\"icon\" type=\"image/png\" href=\"./favicon-16x16.png\" sizes=\"16x16\" />",
                                "<link rel=\"icon\" type=\"image/png\" href=\"https://dev.budgie.co.in/budgie_3/favicon.ico\" sizes=\"16x16\" />");

                return new TransformedResource(resource, html.getBytes());
            } // AutoCloseable br > isr > is
        } else if (resource.toString().contains("swagger-ui.css")) {
            final InputStream is = resource.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            try (BufferedReader br = new BufferedReader(isr)) {
                String css = br.lines().collect(Collectors.joining());
                final byte[] transformedContent = css.replace("1b1b1b", "eff6f4").getBytes();

                return new TransformedResource(resource, transformedContent);
            }
        } else if (resource.toString().contains("index.css")) {
            final InputStream is = resource.getInputStream();
            final InputStreamReader isr = new InputStreamReader(is);
            try (BufferedReader br = new BufferedReader(isr)) {
                String css = br.lines().collect(Collectors.joining());
                css = css
                        + ".topbar-wrapper img {content: url(https://dev.budgie.co.in/budgie_3/static/media/Budgie_Logo.939f78103985bbe80f99.png\r\n);}";
                return new TransformedResource(resource, css.getBytes());
            }
        }

        return super.transform(request, resource, transformerChain);
    }

}