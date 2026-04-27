package com.comercial.crm.domain.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContactRepository extends JpaRepository<Contact, UUID> {

  List<Contact> findByClientIdOrderByIsPrimaryDescFullNameAsc(UUID clientId);

  Optional<Contact> findByClientIdAndIsPrimaryTrue(UUID clientId);

  boolean existsByClientId(UUID clientId);

  /** Quita el flag isPrimary de todos los contactos del cliente antes de asignar uno nuevo. */
  @Modifying
  @Query("UPDATE Contact c SET c.isPrimary = false WHERE c.client.id = :clientId")
  void clearPrimaryByClientId(@Param("clientId") UUID clientId);

  long countByClientId(UUID clientId);
}
