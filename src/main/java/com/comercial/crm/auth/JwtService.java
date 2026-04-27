package com.comercial.crm.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

  private final Key key;
  private final long expirationMinutes;

  public JwtService(
      @Value("${app.security.jwt.secret}") String secret,
      @Value("${app.security.jwt.expiration-minutes}") long expirationMinutes
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expirationMinutes = expirationMinutes;
  }

  public String generateToken(String subject, List<String> roles) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(expirationMinutes * 60);

    return Jwts.builder()
        .subject(subject)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .claim("roles", roles)
        .signWith(key)
        .compact();
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (Exception ex) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith((javax.crypto.SecretKey) key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}