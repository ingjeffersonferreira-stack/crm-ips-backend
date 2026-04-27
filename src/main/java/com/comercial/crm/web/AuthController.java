package com.comercial.crm.web;

import com.comercial.crm.auth.JwtService;
import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final UserRepository userRepository;

  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
    Authentication auth = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(req.email(), req.password())
    );

    List<String> roles = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .toList();

    String token = jwtService.generateToken(req.email(), roles);

    return ResponseEntity.ok(new LoginResponse(token, req.email(), roles));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();

    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

    return ResponseEntity.ok(
        java.util.Map.of(
            "email", user.getEmail(),
            "fullName", user.getFullName(),
            "roles", user.getRoles().stream().map(r -> r.getCode()).toList()
        )
    );
  }
}