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

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

  public static final String AUTHORIZATION = "Authorization";
  public static final String BEARER = "Bearer ";

  private final JwtHelper jwtHelper;

  public JwtFilter(JwtHelper jwtHelper) {
    this.jwtHelper = jwtHelper;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(AUTHORIZATION);

    if (header == null || !header.startsWith(BEARER)) {
      filterChain.doFilter(request, response);
      return;
    }

    User user = this.jwtHelper.extractUser(header.replace(BEARER, ""));

    Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    filterChain.doFilter(request, response);
  }

}
