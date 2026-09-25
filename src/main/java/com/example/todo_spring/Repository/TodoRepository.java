package com.example.todo_spring.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo_spring.Entitiy.TodoEntity;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
  // Derived query: WHERE todo.user.id = :userId
  List<TodoEntity> findByUserId(Long userId);
}
