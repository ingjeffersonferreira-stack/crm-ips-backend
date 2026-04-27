package com.comercial.crm.domain.action;

import com.comercial.crm.domain.client.Client;
import com.comercial.crm.domain.contact.Contact;
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
@Table(
    name = "actions",
    indexes = {
        @Index(name = "idx_actions_responsible_status_date",
               columnList = "responsible_user_id, status, scheduled_at"),
        @Index(name = "idx_actions_client_date",
               columnList = "client_id, scheduled_at")
    }
)
public class Action {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "contact_id")
  private Contact contact;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "responsible_user_id", nullable = false)
  private User responsibleUser;

  @Column(name = "title", nullable = false, length = 220)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  @Builder.Default
  private ActionType type = ActionType.FOLLOW_UP;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private ActionStatus status = ActionStatus.PENDING;

  @Column(name = "scheduled_at", nullable = false)
  private OffsetDateTime scheduledAt;

  @Column(name = "completed_at")
  private OffsetDateTime completedAt;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @PrePersist
  void prePersist() {
    OffsetDateTime now = OffsetDateTime.now();
    this.createdAt = now;
    this.updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    this.updatedAt = OffsetDateTime.now();
  }
}
