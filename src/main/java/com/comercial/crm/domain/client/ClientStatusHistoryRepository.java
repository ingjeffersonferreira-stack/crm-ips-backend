package com.comercial.crm.domain.client;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientStatusHistoryRepository extends JpaRepository<ClientStatusHistory, UUID> {
  List<ClientStatusHistory> findByClientIdOrderByChangedAtAsc(UUID clientId);
}
