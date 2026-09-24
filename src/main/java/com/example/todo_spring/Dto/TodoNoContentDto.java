package com.example.todo_spring.Dto;

import com.example.todo_spring.Entitiy.TodoEntity;

public record TodoNoContentDto(Long id, String title, boolean isDone) {
  public TodoNoContentDto() {
    this(null, "", false);
  }

  public TodoNoContentDto(TodoEntity todo) {
    this(todo.getId(), todo.getTitle(), todo.isDone());
  }
}
