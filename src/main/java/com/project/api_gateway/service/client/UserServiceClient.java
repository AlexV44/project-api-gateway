package com.project.api_gateway.service.client;

import com.project.api_gateway.dto.request.RegisterRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Component
public class UserServiceClient {

    private final WebClient web;

    public UserServiceClient(@Qualifier("userServiceWebClient") WebClient web) {
        this.web = web;
    }

    public Mono<Void> createUser(String token, String userId, RegisterRequest r) {
        Map<String, Object> body = Map.of(
                "id", userId,
                "name", r.firstName(),
                "surname", r.lastName(),
                "email", r.email(),
                "birthday", r.birthday().toString());

        return web.post()
                .uri("/api/v1/users")
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(5))
                .then();
    }
}