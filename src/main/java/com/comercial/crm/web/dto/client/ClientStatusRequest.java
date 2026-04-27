package com.comercial.crm.web.dto.client;

import com.comercial.crm.domain.client.ClientCommercialStatus;
import jakarta.validation.constraints.NotNull;

public record ClientStatusRequest(
    @NotNull(message = "El estado comercial es obligatorio")
    ClientCommercialStatus status
) {}
