package com.comercial.crm.web.dto.followup;

import com.comercial.crm.domain.followup.Followup;
import com.comercial.crm.domain.followup.FollowupResult;
import com.comercial.crm.domain.followup.FollowupType;
import com.comercial.crm.web.dto.action.ActionResponse;

import java.time.OffsetDateTime;
import java.util.UUID;

public record FollowupResponse(
    UUID id,
    UUID clientId,
    String clientBusinessName,
    UUID contactId,
    String contactFullName,
    UUID userId,
    String userFullName,
    FollowupType type,
    FollowupResult result,
    String notes,
    OffsetDateTime eventAt,
    ActionResponse nextAction,
    OffsetDateTime createdAt
) {
  public static FollowupResponse from(Followup f) {
    return new FollowupResponse(
        f.getId(),
        f.getClient().getId(),
        f.getClient().getBusinessName(),
        f.getContact() != null ? f.getContact().getId() : null,
        f.getContact() != null ? f.getContact().getFullName() : null,
        f.getUser().getId(),
        f.getUser().getFullName(),
        f.getType(),
        f.getResult(),
        f.getNotes(),
        f.getEventAt(),
        f.getNextAction() != null ? ActionResponse.from(f.getNextAction()) : null,
        f.getCreatedAt()
    );
  }
}
