package com.comercial.crm.domain.user;

import com.comercial.crm.web.dto.user.UserRequest;
import com.comercial.crm.web.dto.user.UserResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  public List<UserResponse> findAll() {
    return userRepository.findAll().stream().map(UserResponse::from).toList();
  }

  public UserResponse findById(UUID id) {
    return UserResponse.from(getOrThrow(id));
  }

  @Transactional
  public UserResponse create(UserRequest req) {
    if (userRepository.existsByEmail(req.email())) {
      throw new IllegalArgumentException("Ya existe un usuario con el correo: " + req.email());
    }

    Set<Role> roles = resolveRoles(req.roles());
    OffsetDateTime now = OffsetDateTime.now();

    User user = User.builder()
        .id(UUID.randomUUID())
        .fullName(req.fullName())
        .email(req.email())
        .passwordHash(passwordEncoder.encode(req.password()))
        .status(UserStatus.ACTIVE)
        .roles(roles)
        .createdAt(now)
        .updatedAt(now)
        .build();

    return UserResponse.from(userRepository.save(user));
  }

  @Transactional
  public UserResponse toggleStatus(UUID id) {
    User user = getOrThrow(id);
    user.setStatus(user.getStatus() == UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE);
    user.setUpdatedAt(OffsetDateTime.now());
    return UserResponse.from(userRepository.save(user));
  }

  @Transactional
  public UserResponse resetPassword(UUID id, String newPassword) {
    if (newPassword == null || newPassword.length() < 6) {
      throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
    }
    User user = getOrThrow(id);
    user.setPasswordHash(passwordEncoder.encode(newPassword));
    user.setUpdatedAt(OffsetDateTime.now());
    return UserResponse.from(userRepository.save(user));
  }

  // ---- helpers

  private User getOrThrow(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
  }

  private Set<Role> resolveRoles(Set<String> roleCodes) {
    Set<Role> roles = new HashSet<>();
    if (roleCodes == null || roleCodes.isEmpty()) {
      roleRepository.findByCode("SALES").ifPresent(roles::add);
    } else {
      for (String code : roleCodes) {
        roleRepository.findByCode(code.toUpperCase())
            .ifPresent(roles::add);
      }
    }
    return roles;
  }
}
