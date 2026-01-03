package com.taskflow.backend.security;

import com.taskflow.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
public class JwtService implements JwtServiceImpl {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMinutes * 60_000L;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);

        Set<String> roles = user.getRoles() == null ? Set.of()
                : user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toSet());

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))          // subject = userId
                .claim("email", user.getEmail())
                .claim("roles", List.copyOf(roles))
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String extractEmail(String token) {
        Object email = parseClaims(token).get("email");
        return email == null ? null : email.toString();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims c = parseClaims(token);
            return c.getExpiration() != null && c.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
