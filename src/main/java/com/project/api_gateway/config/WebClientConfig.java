package com.project.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean("keycloakWebClient")
    public WebClient keycloakWebClient(@Value("${keycloak.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }

    @Bean("userServiceWebClient")
    public WebClient userServiceWebClient(@Value("${user-service.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }
}
