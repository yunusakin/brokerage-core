package com.brokerage.core.base.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private final String accessSecret;
    private final String refreshSecret;
    private final long accessTtlMs;
    private final long refreshTtlMs;

    public JwtService(
            @Value("${security.jwt.access-secret}")
            String accessSecret,
            @Value("${security.jwt.refresh-secret}")
            String refreshSecret,
            @Value("${security.jwt.access-ttl-ms}")
            long accessTtlMs,
            @Value("${security.jwt.refresh-ttl-ms}")
            long refreshTtlMs
    ) {
        this.accessSecret = accessSecret;
        this.refreshSecret = refreshSecret;
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    private Key key(String secret) { return Keys.hmacShaKeyFor(secret.getBytes()); }

    public String extractUsername(String token, boolean refresh) {
        return extractClaim(token, Claims::getSubject, refresh);
    }
    public String extractJti(String refreshToken) {
        return extractClaim(refreshToken, c -> (String) c.get("jti"), true);
    }
    public boolean isExpired(String token, boolean refresh) {
        Date exp = extractClaim(token, Claims::getExpiration, refresh);
        return exp.before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver, boolean refresh) {
        return resolver.apply(Jwts.parserBuilder()
                .setSigningKey(refresh ? key(refreshSecret) : key(accessSecret))
                .build()
                .parseClaimsJws(token)
                .getBody());
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .setClaims(Map.of("role", role))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTtlMs))
                .signWith(key(accessSecret), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username, String role, String jti) {
        return Jwts.builder()
                .setClaims(Map.of("role", role, "jti", jti))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTtlMs))
                .signWith(key(refreshSecret), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, String username) {
        return isTokenValid(token, username, false);
    }

    public boolean isTokenValid(String token, String username, boolean refresh) {
        try {
            String sub = extractUsername(token, refresh);
            return sub != null && sub.equals(username) && !isExpired(token, refresh);
        } catch (Exception e) {
            return false;
        }
    }
}