package com.comercial.crm.web.dto.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactRequest(

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 180)
    String fullName,

    @Size(max = 140)
    String jobTitle,

    @Email(message = "El correo no tiene formato válido")
    @Size(max = 180)
    String email,

    @Size(max = 40)
    String phone,

    boolean isPrimary
) {}
