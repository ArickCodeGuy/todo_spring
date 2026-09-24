package com.example.todo_spring.Config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtHelper {
  private static final String ROLES = "roles";

  private final byte[] jwtSecret;
  private final long jwtLifeSpan;

  public JwtHelper(@Value("${jwt.secret}") String jwtSecret, @Value("${jwt.life-span:36000000}") long jwtLifeSpan) {
    this.jwtSecret = jwtSecret.getBytes();
    this.jwtLifeSpan = jwtLifeSpan;
  }

  public String createJwt(String userId, List<String> roles) {
    return JWT.create()
        .withSubject(userId)
        .withArrayClaim(ROLES, roles.toArray(new String[0]))
        .withExpiresAt(new Date(System.currentTimeMillis() + jwtLifeSpan))
        .sign(HMAC512(this.jwtSecret));
  }

  public User extractUser(String token) {
    DecodedJWT jwt = JWT.require(Algorithm.HMAC512(this.jwtSecret))
        .build()
        .verify(token);

    List<SimpleGrantedAuthority> roles = jwt.getClaim(ROLES).asList(String.class).stream()
        .map(SimpleGrantedAuthority::new)
        .toList();

    return new User(jwt.getSubject(), "", roles);
  }
}
