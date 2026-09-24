package com.example.todo_spring.Entitiy;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "TodoUser")
@Data
public class UserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String username;
  private String password;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TodoEntity> todos = new ArrayList<>();

  public UserEntity(Long id, String username, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
  }

  public UserEntity() {
    this.id = 0L;
    this.username = "";
    this.password = "";
  }
}
