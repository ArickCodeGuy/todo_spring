package com.example.todo_spring.Controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.todo_spring.Dto.TodoFullDto;
import com.example.todo_spring.Dto.TodoNoContentDto;
import com.example.todo_spring.Entitiy.TodoEntity;
import com.example.todo_spring.Entitiy.UserEntity;
import com.example.todo_spring.Repository.TodoRepository;
import com.example.todo_spring.Repository.UserRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
  public ResponseEntity<List<TodoNoContentDto>> getTodoLists(@AuthenticationPrincipal UserDetails userDetails) {
    UserEntity user = userRepository.findByUsername(userDetails.getUsername());

    List<TodoEntity> todos = todoRepository.findByUser(user);

    List<TodoNoContentDto> res = new ArrayList<>();
    for (TodoEntity todo : todos)
      res.add(new TodoNoContentDto(todo));

    return ResponseEntity.ok(res);
  }

  @GetMapping("/{id}")
  public ResponseEntity<TodoFullDto> getFullTodo(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long id) {
    Optional<TodoEntity> todoOptional = todoRepository.findById(id);
    if (todoOptional.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = todoOptional.get();
    UserEntity user = userRepository.findByUsername(userDetails.getUsername());
    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok(new TodoFullDto(todo));
  }

  @PostMapping("/create")
  public ResponseEntity<TodoNoContentDto> createTodo(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestBody TodoFullDto todoFullDto) {
    TodoEntity todo = new TodoEntity();
    todo.setTitle(todoFullDto.title());
    todo.setContent(todoFullDto.content());
    UserEntity user = userRepository.findByUsername(userDetails.getUsername());
    todo.setUser(user);

    TodoEntity todoSaved = todoRepository.save(todo);

    return ResponseEntity.ok(new TodoNoContentDto(todoSaved));
  }

  @PutMapping("/update/{id}")
  public ResponseEntity<TodoFullDto> updateTodo(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long id, @RequestBody TodoFullDto todoFullDto) {

    Optional<TodoEntity> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = optionalTodo.get();
    UserEntity user = userRepository.findByUsername(userDetails.getUsername());
    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.badRequest().build();
    }

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
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long id) {
    Optional<TodoEntity> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = optionalTodo.get();
    UserEntity user = userRepository.findByUsername(userDetails.getUsername());
    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.badRequest().build();
    }

    TodoEntity todoSaved = todoRepository.save(todo);
    return ResponseEntity.ok(new TodoNoContentDto(todoSaved));
  }

  @DeleteMapping("/delete/{id}")
  public ResponseEntity<TodoNoContentDto> deleteTodo(
      @AuthenticationPrincipal UserDetails userDetails,
      @PathVariable Long id) {
    Optional<TodoEntity> optionalTodo = todoRepository.findById(id);
    if (optionalTodo.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TodoEntity todo = optionalTodo.get();
    UserEntity user = userRepository.findByUsername(userDetails.getUsername());

    if (!isTodoEntityBelongsToUser(todo, user)) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok(new TodoNoContentDto(todo));
  }

  private boolean isTodoEntityBelongsToUser(TodoEntity todo, UserEntity user) {
    return todo.getUser().getId() == user.getId();
  }
}
