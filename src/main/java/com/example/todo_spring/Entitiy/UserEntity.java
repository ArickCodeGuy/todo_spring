package com.example.todo_spring.Entitiy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

  // Filled in by Hibernate: created_at on insert, updated_at on every update.
  // The column default fills existing rows when ddl-auto=update adds the columns
  // to a table that already has data (NOT NULL would fail otherwise).
  @CreationTimestamp
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @ColumnDefault("CURRENT_TIMESTAMP")
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TodoEntity> todos = new ArrayList<>();

  public UserEntity(Long id, String username, String password) {
    this.id = id;
    this.username = username;
    this.password = password;
  }
}
