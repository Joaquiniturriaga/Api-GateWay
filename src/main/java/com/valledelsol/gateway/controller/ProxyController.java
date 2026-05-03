package com.valledelsol.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Map;


@RestController
public class ProxyController {

    @Value("${auth.service.url}")
    private String authUrl;

    @Value("${user.service.url}")
    private String userUrl;

    @Value("{report.service.url}")
    private String reportUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    // --- AUTH routes (publicas) ---
    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String,Object> body) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return restTemplate.postForEntity(authUrl + "/api/auth/register", new HttpEntity<>(body,headers), Object.class);

        }catch(HttpClientErrorException e){
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String,Object> body) {
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            return restTemplate.postForEntity(
                authUrl + "/api/auth/login", new HttpEntity<>(body, headers),
                Object.class
            );

        }catch (HttpClientErrorException e){
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString(null));
        }
    }

    // --- USER routes (protegidas, JwtFilter ya valido el token) ---
    @GetMapping("/api/users/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest req) {
        try{

        
            return restTemplate.exchange(
                userUrl + "/api/users/profile",
                HttpMethod.GET,
                new HttpEntity<>(buildHeaders(req)),
                Object.class
            );
        }catch(HttpClientErrorException e){
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @PutMapping("/api/users/update")
    public ResponseEntity<?> updateUser(HttpServletRequest req,
                                        @RequestBody Map<String,Object> body) {

        try{
            return restTemplate.exchange(
            userUrl + "/api/users/update",
            HttpMethod.PUT,
            new HttpEntity<>(body, buildHeaders(req)),
            Object.class
        );

        }catch(HttpClientErrorException e){
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }

    }


    //reports cabrones

    @PostMapping("/api/reports")
        public ResponseEntity<?> createReport(HttpServletRequest req,
                                            @RequestBody Map<String,Object> body) {
            try {
                return restTemplate.exchange(
                    reportUrl + "/api/reports",
                    HttpMethod.POST,
                    new HttpEntity<>(body, buildHeaders(req)),
                    Object.class
                );
            } catch (HttpClientErrorException e) {
                return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }
        }

        @GetMapping("/api/reports")
        public ResponseEntity<?> getReports(HttpServletRequest req) {
            try {
                return restTemplate.exchange(
                    reportUrl + "/api/reports",
                    HttpMethod.GET,
                    new HttpEntity<>(buildHeaders(req)),
                    Object.class
                );
            } catch (HttpClientErrorException e) {
                return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
            }
        }

        // ─── Health check del gateway ─────────────────────────────────────

        @GetMapping("/")
        public ResponseEntity<?> health() {
            return ResponseEntity.ok(Map.of("status", "API Gateway corriendo"));
        }


    private HttpHeaders buildHeaders(HttpServletRequest req) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-user-id",  String.valueOf(req.getAttribute("userId")));
        headers.set("x-user-rol", String.valueOf(req.getAttribute("userRol")));
        return headers;
    }
}