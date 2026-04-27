package com.comercial.crm.domain.action;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ActionRepository extends JpaRepository<Action, UUID> {

  Page<Action> findByClientId(UUID clientId, Pageable pageable);

  Page<Action> findByResponsibleUserId(UUID userId, Pageable pageable);

  /** Acciones pendientes del usuario autenticado, ordenadas por fecha. */
  List<Action> findByResponsibleUserIdAndStatusOrderByScheduledAtAsc(
      UUID userId, ActionStatus status);

  /** Pendientes de hoy en adelante — para el dashboard. */
  @Query("""
      SELECT a FROM Action a
      WHERE a.status = 'PENDING'
        AND a.scheduledAt >= :from
      ORDER BY a.scheduledAt ASC
      """)
  List<Action> findPendingFrom(@Param("from") OffsetDateTime from, Pageable pageable);

  /** Conteo de pendientes por usuario — KPI del dashboard. */
  long countByResponsibleUserIdAndStatus(UUID userId, ActionStatus status);

  /** KPI global: total pendientes. */
  long countByStatus(ActionStatus status);

  /**
   * Clientes sin ninguna acción pendiente — para el KPI "Clientes Sin Seguimiento".
   * Devuelve IDs de clientes que NO tienen acciones PENDING.
   */
  @Query("""
      SELECT DISTINCT a.client.id FROM Action a
      WHERE a.status = 'PENDING'
      """)
  List<UUID> findClientIdsWithPendingActions();
}
