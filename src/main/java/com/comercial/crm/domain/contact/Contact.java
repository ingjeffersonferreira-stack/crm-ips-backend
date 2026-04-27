package com.comercial.crm.domain.contact;

import com.comercial.crm.domain.client.Client;
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
    name = "contacts",
    indexes = {
        @Index(name = "idx_contacts_client", columnList = "client_id")
    },
    uniqueConstraints = {
        // Solo un contacto principal por cliente (enforced también a nivel lógico en el service)
        @UniqueConstraint(name = "ux_contacts_primary_per_client",
            columnNames = {"client_id", "is_primary"})
    }
)
public class Contact {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "client_id", nullable = false)
  private Client client;

  @Column(name = "full_name", nullable = false, length = 180)
  private String fullName;

  @Column(name = "job_title", length = 140)
  private String jobTitle;

  @Column(name = "email", length = 180)
  private String email;

  @Column(name = "phone", length = 40)
  private String phone;

  @Column(name = "is_primary", nullable = false)
  @Builder.Default
  private boolean isPrimary = false;

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
