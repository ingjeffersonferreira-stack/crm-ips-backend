package com.comercial.crm.web.dto.client;

import com.comercial.crm.domain.client.ClientCommercialStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Used for both create (POST) and full update (PUT).
 * For partial update (PATCH) all fields are optional — validated in the service.
 */
public record ClientRequest(

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 220)
    String businessName,

    @Size(max = 40)
    String nit,

    @Email(message = "El correo no tiene formato válido")
    @Size(max = 180)
    String mainEmail,

    @Size(max = 40)
    String mainPhone,

    @Size(max = 240)
    String address,

    @Size(max = 120)
    String city,

    ClientCommercialStatus commercialStatus,

    /** UUID of the owning commercial user (optional — defaults to current user). */
    UUID ownerUserId
) {}
