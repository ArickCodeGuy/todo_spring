package com.example.todo_spring.Controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.example.todo_spring.Entitiy.TodoEntity;
import com.example.todo_spring.Entitiy.UserEntity;

class TodoControllerTest extends ApiTestBase {

  private String aliceToken;
  private String bobToken;

  @BeforeEach
  void createUsers() throws Exception {
    aliceToken = signupAndGetToken("alice", "alice_password");
    bobToken = signupAndGetToken("bob", "bob_password");
  }

  private TodoEntity findTodo(Long id) {
    return todoRepository.findById(id).orElseThrow();
  }

  @Nested
  @DisplayName("POST /api/v1/todo/create")
  class Create {

    @Test
    @DisplayName("creates a todo owned by the current user")
    void createTodoSuccess() throws Exception {
      mockMvc.perform(post("/api/v1/todo/create")
          .header("Authorization", bearer(aliceToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(todoJson("Buy milk", "2 liters", false)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").isNumber())
          .andExpect(jsonPath("$.title").value("Buy milk"));

      UserEntity alice = userRepository.findByUsername("alice");
      List<TodoEntity> todos = todoRepository.findByUserId(alice.getId());
      assertThat(todos).hasSize(1);
      assertThat(todos.get(0).getTitle()).isEqualTo("Buy milk");
      assertThat(todos.get(0).getContent()).isEqualTo("2 liters");
      assertThat(todos.get(0).isDone()).isFalse();
    }

    @Test
    @DisplayName("new todo shows up only in its owner's list")
    void createdTodoIsListedForOwnerOnly() throws Exception {
      createTodo(aliceToken, "Alice's todo", "content");

      mockMvc.perform(get("/api/v1/todo/all").header("Authorization", bearer(aliceToken)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(1))
          .andExpect(jsonPath("$[0].title").value("Alice's todo"));

      mockMvc.perform(get("/api/v1/todo/all").header("Authorization", bearer(bobToken)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("returns 401 without a token")
    void createTodoUnauthorized() throws Exception {
      mockMvc.perform(post("/api/v1/todo/create")
          .contentType(MediaType.APPLICATION_JSON)
          .content(todoJson("Buy milk", "2 liters", false)))
          .andExpect(status().isUnauthorized());

      assertThat(todoRepository.count()).isZero();
    }
  }

  @Nested
  @DisplayName("PUT /api/v1/todo/update/{id}")
  class Update {

    @Test
    @DisplayName("updates title and content")
    void updateTodoSuccess() throws Exception {
      Long id = createTodo(aliceToken, "Old title", "old content");

      mockMvc.perform(put("/api/v1/todo/update/" + id)
          .header("Authorization", bearer(aliceToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(todoJson("New title", "new content", false)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id))
          .andExpect(jsonPath("$.title").value("New title"))
          .andExpect(jsonPath("$.content").value("new content"));

      TodoEntity todo = findTodo(id);
      assertThat(todo.getTitle()).isEqualTo("New title");
      assertThat(todo.getContent()).isEqualTo("new content");
    }

    @Test
    @DisplayName("returns 403 when updating another user's todo")
    void updateOtherUsersTodo() throws Exception {
      Long id = createTodo(aliceToken, "Alice's todo", "private");

      mockMvc.perform(put("/api/v1/todo/update/" + id)
          .header("Authorization", bearer(bobToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(todoJson("Hacked", "hacked", false)))
          .andExpect(status().isForbidden());

      assertThat(findTodo(id).getTitle()).isEqualTo("Alice's todo");
    }

    @Test
    @DisplayName("returns 400 when the todo is already done")
    void updateDoneTodo() throws Exception {
      Long id = createTodo(aliceToken, "Title", "content");
      mockMvc.perform(post("/api/v1/todo/done/" + id).header("Authorization", bearer(aliceToken)))
          .andExpect(status().isOk());

      mockMvc.perform(put("/api/v1/todo/update/" + id)
          .header("Authorization", bearer(aliceToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(todoJson("Changed", "changed", false)))
          .andExpect(status().isBadRequest());

      assertThat(findTodo(id).getTitle()).isEqualTo("Title");
    }

    @Test
    @DisplayName("returns 404 for a missing todo")
    void updateMissingTodo() throws Exception {
      mockMvc.perform(put("/api/v1/todo/update/999999")
          .header("Authorization", bearer(aliceToken))
          .contentType(MediaType.APPLICATION_JSON)
          .content(todoJson("x", "y", false)))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  @DisplayName("POST /api/v1/todo/done/{id}")
  class MarkAsDone {

    @Test
    @DisplayName("marks the todo as done")
    void markAsDoneSuccess() throws Exception {
      Long id = createTodo(aliceToken, "Title", "content");

      mockMvc.perform(post("/api/v1/todo/done/" + id).header("Authorization", bearer(aliceToken)))
          .andExpect(status().isOk());

      assertThat(findTodo(id).isDone()).isTrue();
    }

    @Test
    @DisplayName("returns 403 for another user's todo")
    void markOtherUsersTodo() throws Exception {
      Long id = createTodo(aliceToken, "Title", "content");

      mockMvc.perform(post("/api/v1/todo/done/" + id).header("Authorization", bearer(bobToken)))
          .andExpect(status().isForbidden());

      assertThat(findTodo(id).isDone()).isFalse();
    }
  }

  @Nested
  @DisplayName("DELETE /api/v1/todo/delete/{id}")
  class Delete {

    @Test
    @DisplayName("deletes the todo")
    void deleteTodoSuccess() throws Exception {
      Long id = createTodo(aliceToken, "To delete", "content");

      mockMvc.perform(delete("/api/v1/todo/delete/" + id).header("Authorization", bearer(aliceToken)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(id));

      assertThat(todoRepository.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("returns 403 and keeps another user's todo")
    void deleteOtherUsersTodo() throws Exception {
      Long id = createTodo(aliceToken, "Alice's todo", "content");

      mockMvc.perform(delete("/api/v1/todo/delete/" + id).header("Authorization", bearer(bobToken)))
          .andExpect(status().isForbidden());

      assertThat(todoRepository.existsById(id)).isTrue();
    }

    @Test
    @DisplayName("returns 404 for a missing todo")
    void deleteMissingTodo() throws Exception {
      mockMvc.perform(delete("/api/v1/todo/delete/999999").header("Authorization", bearer(aliceToken)))
          .andExpect(status().isNotFound());
    }
  }
}
