package com.comercial.crm.domain.client;

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
@Table(name = "client_status_history",
       indexes = @Index(name = "idx_status_hist_client", columnList = "client_id, changed_at"))
public class ClientStatusHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @Enumerated(EnumType.STRING)
  @Column(name = "from_status", length = 30)
  private ClientCommercialStatus fromStatus;  // null = primera asignación

  @Enumerated(EnumType.STRING)
  @Column(name = "to_status", nullable = false, length = 30)
  private ClientCommercialStatus toStatus;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "changed_by_user_id")
  private User changedBy;

  @Column(name = "changed_at", nullable = false)
  private OffsetDateTime changedAt;
}
