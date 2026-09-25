package com.example.todo_spring.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.example.todo_spring.Config.AuthUser;
import com.example.todo_spring.Dto.TodoFullDto;
import com.example.todo_spring.Dto.TodoNoContentDto;
import com.example.todo_spring.Entitiy.TodoEntity;
import com.example.todo_spring.Repository.TodoRepository;
import com.example.todo_spring.Repository.UserRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

// All endpoints here require authentication (SecurityConfig), and JwtFilter only
// authenticates users that exist in the database, so `user` is never null.
@RestController
@RequestMapping(value = "/api/v1/todo")
public class TodoController {
  private final TodoRepository todoRepository;
  private final UserRepository userRepository;

  public TodoController(TodoRepository todoRepository, UserRepository userRepository) {
    this.todoRepository = todoRepository;
    this.userRepository = userRepository;
  }

  @GetMapping("/all")
  public ResponseEntity<List<TodoNoContentDto>> getTodoLists(@AuthenticationPrincipal AuthUser user) {
    List<TodoNoContentDto> res = todoRepository.findByUserId(user.id()).stream()
        .map(TodoNoContentDto::new)
        .toList();

    return ResponseEntity.ok(res);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TodoFullDto> getFullTodo(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long id) {
    Optional<TodoEntity> todoOptional = todoRepository.findById(id);
    if (todoOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = todoOptional.get();
    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    return ResponseEntity.ok(new TodoFullDto(todo));
  }

  @PostMapping("/create")
  public ResponseEntity<TodoNoContentDto> createTodo(
      @AuthenticationPrincipal AuthUser user,
      @RequestBody TodoFullDto todoFullDto) {
    TodoEntity todo = new TodoEntity();
    todo.setTitle(todoFullDto.title());
    todo.setContent(todoFullDto.content());
    // Reference by id only; no extra SELECT for the user
    todo.setUser(userRepository.getReferenceById(user.id()));
    todo.setDone(todoFullDto.isDone());

    TodoEntity todoSaved = todoRepository.save(todo);

    return ResponseEntity.ok(new TodoNoContentDto(todoSaved));
  }

  @PutMapping("/update/{id}")
  public ResponseEntity<TodoFullDto> updateTodo(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long id, @RequestBody TodoFullDto todoFullDto) {
    Optional<TodoEntity> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = optionalTodo.get();
    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    // A finished todo is read only
    if (todo.isDone()) {
      return ResponseEntity.badRequest().build();
    }

    todo.setTitle(todoFullDto.title());
    todo.setContent(todoFullDto.content());

    TodoEntity todoSaved = todoRepository.save(todo);
    return ResponseEntity.ok(new TodoFullDto(todoSaved));
  }

  @PostMapping("/done/{id}")
  public ResponseEntity<TodoNoContentDto> markAsDone(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long id) {
    Optional<TodoEntity> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = optionalTodo.get();
    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    todo.setDone(true);

    TodoEntity todoSaved = todoRepository.save(todo);
    return ResponseEntity.ok(new TodoNoContentDto(todoSaved));
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<TodoNoContentDto> deleteTodo(
      @AuthenticationPrincipal AuthUser user,
      @PathVariable Long id) {
    Optional<TodoEntity> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = optionalTodo.get();
    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    TodoNoContentDto deleted = new TodoNoContentDto(todo);
    todoRepository.delete(todo);

    return ResponseEntity.ok(deleted);
  }

  private boolean isTodoEntityBelongsToUser(TodoEntity todo, AuthUser user) {
    // Long ids must be compared with equals(): `==` compares object references
    // and is only accidentally true for small values (-128..127) that Java caches.
    return todo.getUser() != null && Objects.equals(todo.getUser().getId(), user.id());
  }
}
