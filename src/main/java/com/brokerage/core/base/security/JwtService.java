package com.brokerage.core.base.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private static final String ACCESS_SECRET  = "access_secret_change_me_256_bits_min_access";
    private static final String REFRESH_SECRET = "refresh_secret_change_me_256_bits_min_refresh";
    private static final long ACCESS_TTL_MS    = 1000L * 60 * 15;           // 15 min
    private static final long REFRESH_TTL_MS   = 1000L * 60 * 60 * 24 * 7;  // 7 days

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
                .setSigningKey(refresh ? key(REFRESH_SECRET) : key(ACCESS_SECRET))
                .build()
                .parseClaimsJws(token)
                .getBody());
    }

    public String generateAccessToken(String username, String role) {
        return Jwts.builder()
                .setClaims(Map.of("role", role))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TTL_MS))
                .signWith(key(ACCESS_SECRET), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username, String role, String jti) {
        return Jwts.builder()
                .setClaims(Map.of("role", role, "jti", jti))
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TTL_MS))
                .signWith(key(REFRESH_SECRET), SignatureAlgorithm.HS256)
                .compact();
    }

    // compatibility helper for access tokens (refresh=false)
    public boolean isTokenValid(String token, String username) {
        return isTokenValid(token, username, false);
    }

    // explicit variant for access/refresh
    public boolean isTokenValid(String token, String username, boolean refresh) {
        try {
            String sub = extractUsername(token, refresh);
            return sub != null && sub.equals(username) && !isExpired(token, refresh);
        } catch (Exception e) {
            return false;
        }
    }
}