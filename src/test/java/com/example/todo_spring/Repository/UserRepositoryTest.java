package com.example.todo_spring.Repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

import com.example.todo_spring.Entitiy.UserEntity;

@DataJpaTest
public class UserRepositoryTest {
  @Autowired
  private UserRepository userRepository;

  @Test
  @DisplayName("Should return true when user exists. UserRepositoryTest.testExistById")
  public void testExistsById() {
    UserEntity userEntity = new UserEntity(null, "test_user", "test_password");
    UserEntity savedUser = userRepository.save(userEntity);

    boolean exists = userRepository.existsById(savedUser.getId());
    assertThat(exists).isTrue();
  }
}
