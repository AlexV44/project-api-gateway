package com.project.api_gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.micrometer.metrics.autoconfigure.MetricsProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

public class WebClientConfig {

    @Bean("keycloakWebClient")
    public WebClient keycloackWebClient(@Value("${keycloack.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }

    @Bean("userServiceWebClient")
    public WebClient userServiceWebClient(@Value("${user-service.url}") String url) {
        return WebClient.builder().baseUrl(url).build();
    }
}
