package com.hepl.budgie.config.webClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient helpDeskWebClient(WebClient.Builder builder) {
        return builder.baseUrl("http://127.0.0.1:8000").build();
//        return builder.baseUrl("https://hepl.budgie.co.in/").build();
    }
}
