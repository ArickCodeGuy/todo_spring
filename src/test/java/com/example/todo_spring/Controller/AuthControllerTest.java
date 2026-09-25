package com.example.todo_spring.Controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.todo_spring.Entitiy.UserEntity;

class AuthControllerTest extends ApiTestBase {

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;

  @Nested
  @DisplayName("POST /api/v1/auth/signup")
  class Signup {

    @Test
    @DisplayName("creates the user, stores a hashed password and returns a token")
    void signupCreatesUser() throws Exception {
      mockMvc.perform(post("/api/v1/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content(authJson("alice", "secret123")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").isNotEmpty());

      UserEntity saved = userRepository.findByUsername("alice");
      assertThat(saved).isNotNull();
      assertThat(saved.getPassword()).isNotEqualTo("secret123");
      assertThat(passwordEncoder.matches("secret123", saved.getPassword())).isTrue();
    }

    @Test
    @DisplayName("returned token can be used to call protected endpoints")
    void signupTokenIsValid() throws Exception {
      String token = signupAndGetToken("alice", "secret123");

      mockMvc.perform(get("/api/v1/todo/all").header("Authorization", bearer(token)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("trims the username")
    void signupTrimsUsername() throws Exception {
      String token = signupAndGetToken("  alice  ", "secret123");

      assertThat(userRepository.existsByUsername("alice")).isTrue();
      mockMvc.perform(get("/api/v1/todo/all").header("Authorization", bearer(token)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 409 when the username is already taken")
    void signupDuplicateUsername() throws Exception {
      signupAndGetToken("alice", "secret123");

      mockMvc.perform(post("/api/v1/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content(authJson("alice", "another")))
          .andExpect(status().isConflict());

      assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("returns 400 when username or password is missing")
    void signupMissingFields() throws Exception {
      mockMvc.perform(post("/api/v1/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content("{\"username\":\"alice\"}"))
          .andExpect(status().isBadRequest());

      mockMvc.perform(post("/api/v1/auth/signup")
          .contentType(MediaType.APPLICATION_JSON)
          .content(authJson("   ", "secret123")))
          .andExpect(status().isBadRequest());

      assertThat(userRepository.count()).isZero();
    }
  }

  @Nested
  @DisplayName("POST /api/v1/auth/login")
  class Login {

    @Test
    @DisplayName("returns a working token for correct credentials")
    void loginSuccess() throws Exception {
      signupAndGetToken("bob", "p@ssw0rd");

      String body = mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(authJson("bob", "p@ssw0rd")))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.token").isNotEmpty())
          .andReturn().getResponse().getContentAsString();

      String token = com.jayway.jsonpath.JsonPath.read(body, "$.token");
      mockMvc.perform(get("/api/v1/todo/all").header("Authorization", bearer(token)))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("returns 401 for a wrong password")
    void loginWrongPassword() throws Exception {
      signupAndGetToken("bob", "p@ssw0rd");

      mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(authJson("bob", "wrong")))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("returns 401 for an unknown user")
    void loginUnknownUser() throws Exception {
      mockMvc.perform(post("/api/v1/auth/login")
          .contentType(MediaType.APPLICATION_JSON)
          .content(authJson("nobody", "whatever")))
          .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  @DisplayName("JWT handling")
  class Jwt {

    @Test
    @DisplayName("protected endpoint without a token returns 401")
    void noToken() throws Exception {
      mockMvc.perform(get("/api/v1/todo/all"))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("protected endpoint with an invalid token returns 401")
    void invalidToken() throws Exception {
      mockMvc.perform(get("/api/v1/todo/all").header("Authorization", bearer("not.a.valid.token")))
          .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("valid token of a deleted user returns 401")
    void tokenOfDeletedUser() throws Exception {
      String token = signupAndGetToken("alice", "secret123");
      userRepository.delete(userRepository.findByUsername("alice"));

      mockMvc.perform(get("/api/v1/todo/all").header("Authorization", bearer(token)))
          .andExpect(status().isUnauthorized());
    }
  }
}
