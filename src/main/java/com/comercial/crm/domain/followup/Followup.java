package com.comercial.crm.domain.followup;

import com.comercial.crm.domain.action.Action;
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
    name = "followups",
    indexes = {
        @Index(name = "idx_followups_client_event_at", columnList = "client_id, event_at DESC"),
        @Index(name = "idx_followups_user_event_at",   columnList = "user_id, event_at DESC")
    }
)
public class Followup {

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
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private FollowupType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "result", nullable = false, length = 20)
  private FollowupResult result;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "event_at", nullable = false)
  @Builder.Default
  private OffsetDateTime eventAt = OffsetDateTime.now();

  /** Acción que se generó o actualizó como consecuencia de este seguimiento. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "next_action_id")
  private Action nextAction;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @PrePersist
  void prePersist() {
    this.createdAt = OffsetDateTime.now();
  }
}
