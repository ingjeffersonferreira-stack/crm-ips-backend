package com.comercial.crm.domain.push;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {
  Optional<PushSubscription> findByEndpoint(String endpoint);
  List<PushSubscription> findByUserId(UUID userId);
  List<PushSubscription> findAll();
  void deleteByEndpoint(String endpoint);
}
