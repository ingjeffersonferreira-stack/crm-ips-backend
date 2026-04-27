package com.comercial.crm.domain.client;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {

  @Query("""
      SELECT c FROM Client c
      WHERE (:query IS NULL
             OR LOWER(c.businessName) LIKE LOWER(CONCAT('%', CAST(:query AS string), '%'))
             OR LOWER(c.nit)          LIKE LOWER(CONCAT('%', CAST(:query AS string), '%')))
        AND (:status IS NULL OR c.commercialStatus = :status)
      """)
  Page<Client> search(
      @Param("query") String query,
      @Param("status") ClientCommercialStatus status,
      Pageable pageable);

  /** Clients owned by a specific user. */
  Page<Client> findByOwnerUserId(UUID ownerUserId, Pageable pageable);

  /** Count by status — used by dashboard KPIs. */
  long countByCommercialStatus(ClientCommercialStatus status);

  /**
   * Count active clients (not WON or LOST).
   */
  @Query("""
      SELECT COUNT(c) FROM Client c
      WHERE c.commercialStatus NOT IN (
        com.comercial.crm.domain.client.ClientCommercialStatus.WON,
        com.comercial.crm.domain.client.ClientCommercialStatus.LOST
      )
      """)
  long countActive();

  /** Exists by NIT (useful for duplicate validation). */
  boolean existsByNit(String nit);

  /** Exists by NIT excluding a given id (for update validation). */
  boolean existsByNitAndIdNot(String nit, UUID id);
}
