package com.valledelsol.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
public class ProxyController {
//Funciona
    @Value("${auth.service.url}")
    private String authUrl;

    @Value("${user.service.url}")
    private String userUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // --- AUTH routes (publicas) ---
    @PostMapping("/api/auth/registro")
    public ResponseEntity<?> registro(@RequestBody Map<String,Object> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/registro", body, Object.class);
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String,Object> body) {
        return restTemplate.postForEntity(authUrl + "/api/auth/login", body, Object.class);
    }

    // --- USER routes (protegidas, JwtFilter ya valido el token) ---
    @GetMapping("/api/users/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest req) {
        HttpHeaders headers = buildHeaders(req);
        return restTemplate.exchange(
            userUrl + "/api/users/profile",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Object.class
        );
    }

    @PutMapping("/api/users/update")
    public ResponseEntity<?> updateUser(HttpServletRequest req,
                                        @RequestBody Map<String,Object> body) {
        HttpHeaders headers = buildHeaders(req);
        return restTemplate.exchange(
            userUrl + "/api/users/update",
            HttpMethod.PUT,
            new HttpEntity<>(body, headers),
            Object.class
        );
    }

    // Construye headers con los datos del usuario (extraidos del JWT)
    private HttpHeaders buildHeaders(HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-user-id", String.valueOf(req.getAttribute("userId")));
        headers.set("x-user-rol", String.valueOf(req.getAttribute("userRol")));
        return headers;
    }
}