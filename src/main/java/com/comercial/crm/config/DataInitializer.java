package com.comercial.crm.config;

import com.comercial.crm.domain.user.Role;
import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.domain.user.UserStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.comercial.crm.domain.user.RoleRepository;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Inicialización de datos base.
 * Solo actúa si no existe ningún usuario en la BD (primera ejecución).
 *
 * Variables de entorno:
 *   ADMIN_EMAIL    — correo del primer administrador (default: admin@crm.local)
 *   ADMIN_PASSWORD — contraseña del primer administrador (default: Admin2026*)
 *   ADMIN_NAME     — nombre completo (default: Administrador)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.init.admin-email:admin@crm.local}")
  private String adminEmail;

  @Value("${app.init.admin-password:Admin2026*}")
  private String adminPassword;

  @Value("${app.init.admin-name:Administrador}")
  private String adminName;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    seedRoles();
    if (userRepository.count() == 0) {
      seedAdminUser();
    }
  }

  private void seedRoles() {
    upsertRole("ADMIN",  "Administrador");
    upsertRole("SALES",  "Comercial");
  }

  private void upsertRole(String code, String name) {
    roleRepository.findByCode(code).orElseGet(() -> {
      Role role = Role.builder().id(UUID.randomUUID()).code(code).name(name).build();
      return roleRepository.save(role);
    });
  }

  private void seedAdminUser() {
    Role adminRole = roleRepository.findByCode("ADMIN")
        .orElseThrow(() -> new IllegalStateException("Rol ADMIN no encontrado"));

    OffsetDateTime now = OffsetDateTime.now();
    User admin = User.builder()
        .id(UUID.randomUUID())
        .fullName(adminName)
        .email(adminEmail)
        .passwordHash(passwordEncoder.encode(adminPassword))
        .status(UserStatus.ACTIVE)
        .roles(Set.of(adminRole))
        .createdAt(now)
        .updatedAt(now)
        .build();

    userRepository.save(admin);
    log.info("=================================================");
    log.info("  Usuario administrador creado");
    log.info("  Email   : {}", adminEmail);
    log.info("  Password: {}", adminPassword);
    log.info("  ⚠ Cambia la contraseña después del primer login");
    log.info("=================================================");
  }
}
