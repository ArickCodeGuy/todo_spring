package com.example.todo_spring.Dto;

import com.example.todo_spring.Entitiy.TodoEntity;

public record TodoFullDto(Long id, String title, String content, boolean isDone) {
  public TodoFullDto() {
    this(null, "", "", false);
  }

  public TodoFullDto(TodoEntity todo) {
    this(todo.getId(), todo.getTitle(), todo.getContent(), todo.isDone() || false);
  }
}
