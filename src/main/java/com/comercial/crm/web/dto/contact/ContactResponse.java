package com.comercial.crm.web.dto.contact;

import com.comercial.crm.domain.contact.Contact;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ContactResponse(
    UUID id,
    UUID clientId,
    String clientBusinessName,
    String fullName,
    String jobTitle,
    String email,
    String phone,
    boolean isPrimary,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
  public static ContactResponse from(Contact c) {
    return new ContactResponse(
        c.getId(),
        c.getClient().getId(),
        c.getClient().getBusinessName(),
        c.getFullName(),
        c.getJobTitle(),
        c.getEmail(),
        c.getPhone(),
        c.isPrimary(),
        c.getCreatedAt(),
        c.getUpdatedAt()
    );
  }
}
