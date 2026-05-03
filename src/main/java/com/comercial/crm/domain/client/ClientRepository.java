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
        AND (:ownerId IS NULL OR c.ownerUser.id = :ownerId)
      """)
  Page<Client> search(
      @Param("query")   String query,
      @Param("status")  ClientCommercialStatus status,
      @Param("ownerId") UUID ownerId,
      Pageable pageable);

  Page<Client> findByOwnerUserId(UUID ownerUserId, Pageable pageable);

  long countByCommercialStatus(ClientCommercialStatus status);

  @Query("SELECT COUNT(c) FROM Client c WHERE c.commercialStatus NOT IN ('WON', 'LOST')")
long countActive();

  boolean existsByNit(String nit);
  boolean existsByNitAndIdNot(String nit, UUID id);
}
