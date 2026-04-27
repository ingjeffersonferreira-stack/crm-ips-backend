package com.comercial.crm.web.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record UserRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 160)
    String fullName,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no tiene formato válido")
    @Size(max = 180)
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    String password,

    /** Roles: ADMIN, SALES */
    Set<String> roles
) {}
