package com.example.todo_spring.Controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.todo_spring.Repository.TodoRepository;
import com.example.todo_spring.Repository.UserRepository;
import com.jayway.jsonpath.JsonPath;

/**
 * Starts the whole application (with the H2 database from
 * src/test/resources/application.properties) and calls the REST API through
 * MockMvc, including the security filter chain and JWT handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class ApiTestBase {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected UserRepository userRepository;

  @Autowired
  protected TodoRepository todoRepository;

  @BeforeEach
  void cleanDatabase() {
    todoRepository.deleteAll();
    userRepository.deleteAll();
  }

  protected static String authJson(String username, String password) {
    return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
  }

  protected static String todoJson(String title, String content, boolean isDone) {
    return "{\"title\":\"" + title + "\",\"content\":\"" + content + "\",\"isDone\":\"" + isDone + "\"}";
  }

  protected static String bearer(String token) {
    return "Bearer " + token;
  }

  /** Signs up a new user through the API and returns their JWT. */
  protected String signupAndGetToken(String username, String password) throws Exception {
    String body = mockMvc.perform(post("/api/v1/auth/signup")
        .contentType(MediaType.APPLICATION_JSON)
        .content(authJson(username, password)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    return JsonPath.read(body, "$.token");
  }

  /** Creates a todo through the API and returns its id. */
  protected Long createTodo(String token, String title, String content) throws Exception {
    String body = mockMvc.perform(post("/api/v1/todo/create")
        .header("Authorization", bearer(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(todoJson(title, content, false)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    Number id = JsonPath.read(body, "$.id");
    return id.longValue();
  }
}
