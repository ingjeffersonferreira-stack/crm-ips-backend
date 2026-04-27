package com.comercial.crm.web.dto.user;

import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserStatus;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponse(
    UUID id,
    String fullName,
    String email,
    UserStatus status,
    Set<String> roles,
    OffsetDateTime createdAt,
    OffsetDateTime lastLoginAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(
            u.getId(),
            u.getFullName(),
            u.getEmail(),
            u.getStatus(),
            u.getRoles().stream().map(r -> r.getCode()).collect(Collectors.toSet()),
            u.getCreatedAt(),
            u.getLastLoginAt()
        );
    }
}
