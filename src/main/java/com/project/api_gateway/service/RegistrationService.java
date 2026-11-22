package com.project.api_gateway.service;

import com.project.api_gateway.dto.request.RegisterRequest;
import com.project.api_gateway.service.client.KeycloakClient;
import com.project.api_gateway.service.client.UserServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class RegistrationService {

    private static final Logger log = LoggerFactory.getLogger(RegistrationService.class);

    private final KeycloakClient keycloak;
    private final UserServiceClient userService;

    public RegistrationService(KeycloakClient keycloak, UserServiceClient userService) {
        this.keycloak = keycloak;
        this.userService = userService;
    }

    public Mono<Void> register(RegisterRequest request) {
        return keycloak.serviceToken().flatMap(token ->
                keycloak.createUser(token, request).flatMap(userId ->
                        userService.createUser(token, userId, request)
                                .onErrorResume(ex -> {
                                    log.warn("User Service failed for {}, rolling back Keycloak user", userId, ex);
                                    return rollback(token, userId).then(Mono.error(toClientError(ex)));
                                })));
    }

    private Mono<Void> rollback(String token, String userId) {
        return keycloak.deleteUser(token, userId)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(300)))
                .doOnError(e -> log.error("ROLLBACK FAILED: user {} remains in Keycloak, clean up manually", userId, e))
                .onErrorResume(e -> Mono.empty());
    }

    private Throwable toClientError(Throwable ex) {
        if (ex instanceof WebClientResponseException w && w.getStatusCode().is4xxClientError()) {
            return new ResponseStatusException(w.getStatusCode(), "User data rejected by User Service");
        }
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Registration failed, changes were rolled back");
    }
}