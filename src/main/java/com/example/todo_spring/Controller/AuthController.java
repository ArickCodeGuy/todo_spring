package com.example.todo_spring.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.todo_spring.Config.JwtHelper;
import com.example.todo_spring.Dto.AuthRequestDto;
import com.example.todo_spring.Dto.AuthResponseDto;
import com.example.todo_spring.Entitiy.UserEntity;
import com.example.todo_spring.Repository.UserRepository;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserRepository userRepository;
  private final JwtHelper jwtHelper;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;

  public AuthController(UserRepository userRepository, JwtHelper jwtHelper,
      BCryptPasswordEncoder bCryptPasswordEncoder) {
    this.userRepository = userRepository;
    this.jwtHelper = jwtHelper;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto authRequest) {
    Optional<UserEntity> userOptional = userRepository.findUser(authRequest.username(), authRequest.password());

    if (userOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    return ResponseEntity.ok(new AuthResponseDto(jwtHelper.createJwt(authRequest.username(), List.of())));
  }

  @PostMapping("/signup")
  public ResponseEntity<AuthResponseDto> signup(@RequestBody AuthRequestDto authRequest) {
    UserEntity user = new UserEntity(null, authRequest.username().trim(),
        bCryptPasswordEncoder.encode(authRequest.password()));

    if (userRepository.existsByUsername(user.getUsername())) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    userRepository.save(user);

    return ResponseEntity.ok(new AuthResponseDto(jwtHelper.createJwt(authRequest.username(), List.of())));
  }

  public UserEntity getCurrentUser(
      @AuthenticationPrincipal UserDetails userDetails) {
    return userRepository.findByUsername(userDetails.getUsername());
  }
}
