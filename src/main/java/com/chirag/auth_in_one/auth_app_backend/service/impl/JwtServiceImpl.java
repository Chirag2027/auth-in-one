package com.chirag.auth_in_one.auth_app_backend.service.impl;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Service
public class JwtServiceImpl {
    // operations related to jwt will be written here

    private final SecretKey key;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String issuer;

    public JwtServiceImpl(
              @Value("${security.jwt.secret}" ) String secret,
              @Value("${security.jwt.access-ttl-seconds}") long accessTokenTtlSeconds,
              @Value("${security.jwt.refresh-ttl-seconds}") long refreshTokenTtlSeconds,
              @Value("${security.jwt.issuer}") String issuer) {

        if (secret == null || secret.length() < 64) {
            throw new IllegalArgumentException("Invalid JWT Secret");
        }
        this.key= Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.issuer = issuer;
    }
}
