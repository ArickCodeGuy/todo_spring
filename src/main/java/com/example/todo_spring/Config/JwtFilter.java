package com.example.todo_spring.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.example.todo_spring.Entitiy.UserEntity;
import com.example.todo_spring.Repository.UserRepository;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER = "Bearer ";

  private final JwtHelper jwtHelper;
  private final UserRepository userRepository;

  public JwtFilter(JwtHelper jwtHelper, UserRepository userRepository) {
    this.jwtHelper = jwtHelper;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(AUTHORIZATION);

    if (header == null || !header.startsWith(BEARER)) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      User tokenUser = this.jwtHelper.extractUser(header.substring(BEARER.length()).trim());

      // A token stays valid until it expires, even if its user was deleted or
      // renamed. Only authenticate users that still exist in the database.
      UserEntity user = userRepository.findByUsername(tokenUser.getUsername());
      if (user != null) {
        AuthUser principal = new AuthUser(user.getId(), user.getUsername());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal, null, tokenUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      // Otherwise the request stays unauthenticated and protected endpoints return 401.
    } catch (JWTVerificationException e) {
      // Invalid, tampered or expired token: continue unauthenticated so the
      // request gets a 401 instead of an unhandled exception (500).
      SecurityContextHolder.clearContext();
    }

    filterChain.doFilter(request, response);
  }

}
