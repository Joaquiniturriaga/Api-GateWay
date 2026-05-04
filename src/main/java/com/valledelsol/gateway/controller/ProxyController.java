package com.valledelsol.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.*;
import java.util.Map;

@RestController
public class ProxyController {

    @Value("${auth.service.url}")
    private String authUrl;

    @Value("${user.service.url}")
    private String userUrl;

    @Value("${report.service.url}")
    private String reportUrl;

    private final RestTemplate restTemplate;

    public ProxyController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);

        RestTemplate rt = new RestTemplate(factory);

        rt.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(HttpStatusCode code) {
                return false; 
            }
        });

        this.restTemplate = rt;
    }

    // ── AUTH público ──────────────────────────────────────────────

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String,Object> body) {
        System.out.println(">>> register llamando a: " + authUrl);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(
            authUrl + "/api/auth/register",
            new HttpEntity<>(body, h),
            Object.class
        );
    }

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String,Object> body) {
        System.out.println(">>> login llamando a: " + authUrl);
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity(
            authUrl + "/api/auth/login",
            new HttpEntity<>(body, h),
            Object.class
        );
    }

    // ── USERS protegido ───────────────────────────────────────────

    @GetMapping("/api/users/profile")
    public ResponseEntity<?> getProfile(HttpServletRequest req) {
        return restTemplate.exchange(
            userUrl + "/api/users/profile",
            HttpMethod.GET,
            new HttpEntity<>(buildHeaders(req)),
            Object.class
        );
    }

    @PutMapping("/api/users/update")
    public ResponseEntity<?> updateUser(HttpServletRequest req,
                                        @RequestBody Map<String,Object> body) {
        return restTemplate.exchange(
            userUrl + "/api/users/update",
            HttpMethod.PUT,
            new HttpEntity<>(body, buildHeaders(req)),
            Object.class
        );
    }

    // ── REPORTS protegido ─────────────────────────────────────────

    @PostMapping("/api/reports")
    public ResponseEntity<?> createReport(HttpServletRequest req,
                                          @RequestBody Map<String,Object> body) {
        return restTemplate.exchange(
            reportUrl + "/api/reports",
            HttpMethod.POST,
            new HttpEntity<>(body, buildHeaders(req)),
            Object.class
        );
    }

    @GetMapping("/api/reports")
    public ResponseEntity<?> getReports(HttpServletRequest req) {
        return restTemplate.exchange(
            reportUrl + "/api/reports",
            HttpMethod.GET,
            new HttpEntity<>(buildHeaders(req)),
            Object.class
        );
    }

    // ── Health ────────────────────────────────────────────────────

    @GetMapping("/")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "gateway corriendo"));
    }

    // ── Helpers ───────────────────────────────────────────────────

    private HttpHeaders buildHeaders(HttpServletRequest req) {
        HttpHeaders h = new HttpHeaders();
        h.set("x-user-id",  String.valueOf(req.getAttribute("userId")));
        h.set("x-user-rol", String.valueOf(req.getAttribute("userRol")));
        return h;
    }
}