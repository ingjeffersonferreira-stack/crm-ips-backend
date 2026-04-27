package com.comercial.crm.domain.user;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "code", nullable = false, unique = true, length = 40)
  private String code; // ADMIN, SALES

  @Column(name = "name", nullable = false, length = 120)
  private String name;
}