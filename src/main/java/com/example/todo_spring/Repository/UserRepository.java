package com.example.todo_spring.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.todo_spring.Entitiy.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

  @Query(value = "select * from todo_user where 'username' = :username and password_hash = crypt(:password, password_hash)", nativeQuery = true)
  Optional<UserEntity> findUser(String username, String password);

  boolean existsByUsername(String username);

  UserEntity findByUsername(String username);
}
