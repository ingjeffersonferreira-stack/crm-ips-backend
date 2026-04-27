package com.comercial.crm.web.dto.action;

import com.comercial.crm.domain.action.ActionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActionRequest(

    @NotBlank(message = "El título de la acción es obligatorio")
    @Size(max = 220)
    String title,

    @NotNull(message = "El tipo de acción es obligatorio")
    ActionType type,

    @NotNull(message = "La fecha programada es obligatoria")
    OffsetDateTime scheduledAt,

    /** Contacto asociado (opcional) */
    UUID contactId,

    /** Responsable (opcional — defecto: usuario autenticado) */
    UUID responsibleUserId
) {}
