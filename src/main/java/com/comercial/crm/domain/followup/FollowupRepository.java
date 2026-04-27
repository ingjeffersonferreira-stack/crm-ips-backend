package com.comercial.crm.domain.followup;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface FollowupRepository extends JpaRepository<Followup, UUID> {

  Page<Followup> findByClientIdOrderByEventAtDesc(UUID clientId, Pageable pageable);

  Page<Followup> findByUserIdOrderByEventAtDesc(UUID userId, Pageable pageable);

  /** Seguimientos de hoy (para el KPI "Contactos Realizados Hoy"). */
  @Query("""
      SELECT COUNT(f) FROM Followup f
      WHERE f.eventAt >= :startOfDay AND f.eventAt < :endOfDay
      """)
  long countTodayFollowups(
      @Param("startOfDay") OffsetDateTime startOfDay,
      @Param("endOfDay") OffsetDateTime endOfDay
  );

  /** IDs de clientes con al menos un seguimiento en los últimos N días. */
  @Query("""
      SELECT DISTINCT f.client.id FROM Followup f
      WHERE f.eventAt >= :since
      """)
  List<UUID> findClientIdsWithRecentFollowup(@Param("since") OffsetDateTime since);
}
