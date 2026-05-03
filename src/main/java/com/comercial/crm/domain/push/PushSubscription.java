package com.comercial.crm.domain.push;

import com.comercial.crm.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "push_subscriptions",
       uniqueConstraints = @UniqueConstraint(columnNames = "endpoint"))
public class PushSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "endpoint", nullable = false, length = 512)
  private String endpoint;

  @Column(name = "p256dh", nullable = false, length = 256)
  private String p256dh;

  @Column(name = "auth", nullable = false, length = 128)
  private String auth;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() { this.createdAt = OffsetDateTime.now(); }
}
