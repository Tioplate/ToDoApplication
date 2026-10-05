package com.todo.todoapplication.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT utility for token generation, parsing, validation and refresh.
 *
 * The signing secret is injected from the `jwt.secret` property, which is
 * expected to be provided through the JWT_SECRET environment variable.
 */
@Component
public class JwtUtils {

    private static final long EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;

    private final SecretKey secretKey;

    public JwtUtils(@Value("${jwt.secret}") String secret) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public String generateToken(String username, Map<String, Object> claims) {
        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public Object getClaim(String token, String claimKey) {
        return parseToken(token).get(claimKey);
    }

    public Date getExpiration(String token) {
        return parseToken(token).getExpiration();
    }

    public boolean isExpired(String token) {
        try {
            return getExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsername(token);
            return tokenUsername.equals(username) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        Claims claims = parseToken(token);
        return Jwts.builder()
                .subject(claims.getSubject())
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(secretKey)
                .compact();
    }
}
