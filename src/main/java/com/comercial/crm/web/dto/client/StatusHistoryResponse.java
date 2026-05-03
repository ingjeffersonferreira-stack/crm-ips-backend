package com.comercial.crm.web.dto.client;

import com.comercial.crm.domain.client.ClientCommercialStatus;
import com.comercial.crm.domain.client.ClientStatusHistory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StatusHistoryResponse(
    UUID id,
    ClientCommercialStatus fromStatus,
    ClientCommercialStatus toStatus,
    String changedByName,
    OffsetDateTime changedAt
) {
  public static StatusHistoryResponse from(ClientStatusHistory h) {
    return new StatusHistoryResponse(
        h.getId(),
        h.getFromStatus(),
        h.getToStatus(),
        h.getChangedBy() != null ? h.getChangedBy().getFullName() : "Sistema",
        h.getChangedAt()
    );
  }
}
