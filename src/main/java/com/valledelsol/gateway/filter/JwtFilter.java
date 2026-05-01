package com.valledelsol.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    // Rutas que NO necesitan token
    private static final List<String> RUTAS_PUBLICAS = List.of(
        "/api/auth/registro",
        "/api/auth/login",
        "/"
    );


   @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();

        if (RUTAS_PUBLICAS.contains(path)) {
            chain.doFilter(req, res);
            return;
        }

        String authHeader = req.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Token requerido\"}");
            return;
        }

        String token = authHeader.substring(7);

        try {
            Key key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
            );
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

            // Inyectar datos del usuario para los microservicios
            req.setAttribute("userId", claims.get("id"));
            req.setAttribute("userRol", claims.get("rol"));

        } catch (JwtException e) {
            res.setStatus(401);
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Token invalido o expirado\"}");
            return;
        }

        chain.doFilter(req, res);
    }
}