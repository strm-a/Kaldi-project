package com.kaldi.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class TokenService {

    public String generateToken(String username, String role) {
        return Jwt.issuer("kaldi-api")
                .subject(username)
                .groups(Set.of(role))
                .expiresIn(Duration.ofHours(8))
                .sign();
    }
}