package com.example.todo_spring.Entitiy;

import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

/**
 * Common columns for every entity. @MappedSuperclass is not a table itself:
 * these fields become columns of each subclass's own table.
 *
 * Only getters: all fields are filled in by the database/Hibernate, so
 * application code should never set them.
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity {
  // Stays null for new entities, so Spring Data calls persist() instead of merge()
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

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
}
