package com.example.todo_spring.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo_spring.Entitiy.TodoEntity;
import com.example.todo_spring.Entitiy.UserEntity;

import java.util.List;

public interface TodoRepository extends JpaRepository<TodoEntity, Long> {
  List<TodoEntity> findByUser(UserEntity user);
}
