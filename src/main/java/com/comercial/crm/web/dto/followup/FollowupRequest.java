package com.comercial.crm.web.dto.followup;

import com.comercial.crm.domain.followup.FollowupResult;
import com.comercial.crm.domain.followup.FollowupType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FollowupRequest(

    @NotNull(message = "El tipo de seguimiento es obligatorio")
    FollowupType type,

    @NotNull(message = "El resultado del seguimiento es obligatorio")
    FollowupResult result,

    @Size(max = 5000, message = "Las observaciones no pueden superar 5000 caracteres")
    String notes,

    /** Cuándo ocurrió — si es null se usa now(). */
    OffsetDateTime eventAt,

    /** Contacto asociado (opcional). */
    UUID contactId,

    /**
     * Si se envía, se crea o actualiza una acción de próximo paso.
     * Puede ser null si no hay próxima acción.
     */
    NextActionRequest nextAction
) {
  public record NextActionRequest(
      @NotNull String title,
      @NotNull com.comercial.crm.domain.action.ActionType type,
      @NotNull OffsetDateTime scheduledAt,
      UUID responsibleUserId
  ) {}
}
