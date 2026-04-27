package com.comercial.crm.web.dto.action;

import com.comercial.crm.domain.action.Action;
import com.comercial.crm.domain.action.ActionStatus;
import com.comercial.crm.domain.action.ActionType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ActionResponse(
    UUID id,
    UUID clientId,
    String clientBusinessName,
    UUID contactId,
    String contactFullName,
    UUID responsibleUserId,
    String responsibleUserFullName,
    String title,
    ActionType type,
    ActionStatus status,
    OffsetDateTime scheduledAt,
    OffsetDateTime completedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
  public static ActionResponse from(Action a) {
    return new ActionResponse(
        a.getId(),
        a.getClient().getId(),
        a.getClient().getBusinessName(),
        a.getContact() != null ? a.getContact().getId() : null,
        a.getContact() != null ? a.getContact().getFullName() : null,
        a.getResponsibleUser().getId(),
        a.getResponsibleUser().getFullName(),
        a.getTitle(),
        a.getType(),
        a.getStatus(),
        a.getScheduledAt(),
        a.getCompletedAt(),
        a.getCreatedAt(),
        a.getUpdatedAt()
    );
  }
}
