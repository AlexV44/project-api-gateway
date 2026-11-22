package com.project.api_gateway.controller;

import com.project.api_gateway.dto.request.LoginRequest;
import com.project.api_gateway.dto.request.RegisterRequest;
import com.project.api_gateway.service.RegistrationService;
import com.project.api_gateway.service.client.KeycloakClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final KeycloakClient keycloak;
    private final RegistrationService registration;

    public AuthController(KeycloakClient keycloak, RegistrationService registration) {
        this.keycloak = keycloak;
        this.registration = registration;
    }

    @PostMapping("/login")
    public Mono<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return keycloak.login(request);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> register(@Valid @RequestBody RegisterRequest request) {
        return registration.register(request);
    }
}
