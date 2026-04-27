package com.comercial.crm.web.dto.client;

import com.comercial.crm.domain.client.Client;
import com.comercial.crm.domain.client.ClientCommercialStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ClientResponse(
    UUID id,
    String businessName,
    String nit,
    String mainEmail,
    String mainPhone,
    String address,
    String city,
    ClientCommercialStatus commercialStatus,
    OwnerSummary ownerUser,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {

  public record OwnerSummary(UUID id, String fullName, String email) {}

  public static ClientResponse from(Client c) {
    OwnerSummary owner = c.getOwnerUser() == null ? null
        : new OwnerSummary(
            c.getOwnerUser().getId(),
            c.getOwnerUser().getFullName(),
            c.getOwnerUser().getEmail()
          );

    return new ClientResponse(
        c.getId(),
        c.getBusinessName(),
        c.getNit(),
        c.getMainEmail(),
        c.getMainPhone(),
        c.getAddress(),
        c.getCity(),
        c.getCommercialStatus(),
        owner,
        c.getCreatedAt(),
        c.getUpdatedAt()
    );
  }
}
