package com.comercial.crm.domain.push;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, UUID> {

  Optional<PushSubscription> findByEndpoint(String endpoint);

  List<PushSubscription> findByUserId(UUID userId);

  @Modifying
  @Query("DELETE FROM PushSubscription p WHERE p.endpoint = :endpoint")
  void deleteByEndpoint(@Param("endpoint") String endpoint);
}