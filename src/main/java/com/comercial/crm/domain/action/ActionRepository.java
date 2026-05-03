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

  Page<Action> findByClientIdOrderByScheduledAtAsc(UUID clientId, Pageable pageable);

  Page<Action> findByClientId(UUID clientId, Pageable pageable);

  long countByStatus(ActionStatus status);

  @Query("""
      SELECT a FROM Action a
      WHERE a.status = :status
        AND a.scheduledAt >= :from
      ORDER BY a.scheduledAt ASC
      """)
  List<Action> findPendingFrom(@Param("from") OffsetDateTime from, Pageable pageable);

  @Query("SELECT DISTINCT a.client.id FROM Action a WHERE a.status = 'PENDING'")
  List<UUID> findClientIdsWithPendingActions();

  /** Para agenda personal — acciones pendientes del usuario autenticado */
  List<Action> findByResponsibleUserIdAndStatusOrderByScheduledAtAsc(
      UUID responsibleUserId, ActionStatus status);
}
