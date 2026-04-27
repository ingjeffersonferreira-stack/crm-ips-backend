package com.comercial.crm.web;

import com.comercial.crm.domain.user.UserService;
import com.comercial.crm.web.dto.user.UserRequest;
import com.comercial.crm.web.dto.user.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class UserController {

  private final UserService userService;

  @GetMapping
  public ResponseEntity<List<UserResponse>> list() {
    return ResponseEntity.ok(userService.findAll());
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(req));
  }

  @PatchMapping("/{id}/toggle-status")
  public ResponseEntity<UserResponse> toggleStatus(@PathVariable UUID id) {
    return ResponseEntity.ok(userService.toggleStatus(id));
  }

  @PatchMapping("/{id}/reset-password")
  public ResponseEntity<UserResponse> resetPassword(
      @PathVariable UUID id,
      @RequestBody Map<String, String> body
  ) {
    return ResponseEntity.ok(userService.resetPassword(id, body.get("password")));
  }
}
