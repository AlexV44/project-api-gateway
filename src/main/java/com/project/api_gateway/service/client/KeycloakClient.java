package com.project.api_gateway.service.client;

import com.project.api_gateway.dto.request.LoginRequest;
import com.project.api_gateway.dto.request.RegisterRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class KeycloakClient {

    private final WebClient web;
    private final String realm, loginClientId, serviceClientId, serviceClientSecret;

    public KeycloakClient(@Qualifier("keycloakWebClient") WebClient web,
                          @Value("${keycloak.realm}") String realm,
                          @Value("${keycloak.login-client-id}") String loginClientId,
                          @Value("${keycloak.service-client-id}") String serviceClientId,
                          @Value("${keycloak.service-client-secret}") String serviceClientSecret) {
        this.web = web;
        this.realm = realm;
        this.loginClientId = loginClientId;
        this.serviceClientId = serviceClientId;
        this.serviceClientSecret = serviceClientSecret;
    }

    public Mono<Map<String, Object>> login(LoginRequest r) {
        return web.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "password")
                        .with("client_id", loginClientId)
                        .with("username", r.username())
                        .with("password", r.password()))
                .retrieve()
                .onStatus(s -> s.value() == 400 || s.value() == 401,
                        resp -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")))
                .bodyToMono(new ParameterizedTypeReference<>() {});
    }

    public Mono<String> serviceToken() {
        return web.post()
                .uri("/realms/{realm}/protocol/openid-connect/token", realm)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", serviceClientId)
                        .with("client_secret", serviceClientSecret))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(m -> (String) m.get("access_token"));
    }

    public Mono<String> createUser(String token, RegisterRequest r) {
        Map<String, Object> body = Map.of(
                "username", r.email(),
                "email", r.email(),
                "firstName", r.firstName(),
                "lastName", r.lastName(),
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(Map.of("type", "password", "value", r.password(), "temporary", false)));

        return web.post()
                .uri("/admin/realms/{realm}/users", realm)
                .headers(h -> h.setBearerAuth(token))
                .bodyValue(body)
                .exchangeToMono(resp -> {
                    int status = resp.statusCode().value();
                    if (status == 201) {
                        String path = resp.headers().asHttpHeaders().getLocation().getPath();
                        return Mono.just(path.substring(path.lastIndexOf('/') + 1));
                    }
                    if (status == 409) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT, "User already exists"));
                    }
                    if (status == 400) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid user data or password policy"));
                    }
                    return resp.createException().flatMap(Mono::error);
                });
    }

    public Mono<Void> deleteUser(String token, String userId) {
        return web.delete()
                .uri("/admin/realms/{realm}/users/{id}", realm, userId)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .toBodilessEntity()
                .then();
    }
}