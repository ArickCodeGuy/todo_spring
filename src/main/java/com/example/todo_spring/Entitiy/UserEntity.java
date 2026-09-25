package com.example.todo_spring.Entitiy;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Data is not used on JPA entities: its equals/hashCode/toString walk the
// user <-> todos relation in both directions and recurse forever.
@Entity
@Table(name = "TodoUser")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {
  // Must stay null for new users. A non-null id (the old default of 0L) makes
  // Spring Data treat the entity as existing and call merge() instead of persist().
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String password;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TodoEntity> todos = new ArrayList<>();

  public UserEntity(Long id, String username, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
  }
}
