package com.comercial.crm.web.dto.action;

import com.comercial.crm.domain.action.ActionStatus;
import jakarta.validation.constraints.NotNull;

public record ActionStatusRequest(
    @NotNull(message = "El estado es obligatorio")
    ActionStatus status
) {}
