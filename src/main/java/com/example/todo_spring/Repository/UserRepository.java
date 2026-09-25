package com.example.todo_spring.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.todo_spring.Entitiy.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  boolean existsByUsername(String username);

  UserEntity findByUsername(String username);
}
