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
@Table(
    name = "clients",
    indexes = {
        @Index(name = "idx_clients_nit", columnList = "nit"),
        @Index(name = "idx_clients_owner_status", columnList = "owner_user_id, commercial_status"),
        @Index(name = "idx_clients_business_name", columnList = "business_name")
    }
)
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "business_name", nullable = false, length = 220)
  private String businessName;

  @Column(name = "nit", length = 40)
  private String nit;

  @Column(name = "main_email", length = 180)
  private String mainEmail;

  @Column(name = "main_phone", length = 40)
  private String mainPhone;

  @Column(name = "address", length = 240)
  private String address;

  @Column(name = "city", length = 120)
  private String city;

  @Enumerated(EnumType.STRING)
  @Column(name = "commercial_status", nullable = false, length = 30)
  @Builder.Default
  private ClientCommercialStatus commercialStatus = ClientCommercialStatus.NEW;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_user_id")
  private User ownerUser;

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
