package com.example.todo_spring.Config;

/**
 * Principal stored in the SecurityContext for authenticated requests.
 * JwtFilter only creates it after checking that the user exists in the database,
 * so controllers can rely on it being non-null and valid.
 *
 * Use in controllers: {@code @AuthenticationPrincipal AuthUser user}
 */
public record AuthUser(Long id, String username) {
}
