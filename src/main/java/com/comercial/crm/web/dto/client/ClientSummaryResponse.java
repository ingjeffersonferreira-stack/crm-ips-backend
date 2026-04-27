package com.comercial.crm.web.dto.client;

import com.comercial.crm.domain.client.Client;
import com.comercial.crm.domain.client.ClientCommercialStatus;

import java.util.UUID;

/** Lightweight projection for paginated lists and search results. */
public record ClientSummaryResponse(
    UUID id,
    String businessName,
    String nit,
    String mainEmail,
    String mainPhone,
    String city,
    ClientCommercialStatus commercialStatus,
    String ownerFullName
) {
  public static ClientSummaryResponse from(Client c) {
    return new ClientSummaryResponse(
        c.getId(),
        c.getBusinessName(),
        c.getNit(),
        c.getMainEmail(),
        c.getMainPhone(),
        c.getCity(),
        c.getCommercialStatus(),
        c.getOwnerUser() != null ? c.getOwnerUser().getFullName() : null
    );
  }
}
