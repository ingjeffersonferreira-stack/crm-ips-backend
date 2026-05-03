package com.comercial.crm.domain.push;

import com.comercial.crm.domain.action.Action;
import com.comercial.crm.domain.action.ActionRepository;
import com.comercial.crm.domain.action.ActionStatus;
import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushService {

  private final PushSubscriptionRepository subRepo;
  private final ActionRepository           actionRepo;
  private final UserRepository             userRepo;
  private final ObjectMapper               objectMapper;

  @Value("${push.vapid.public-key}")
  private String vapidPublicKey;

  @Value("${push.vapid.subject:mailto:admin@sainsa.com}")
  private String vapidSubject;

  // ── Subscribe / Unsubscribe ────────────────────────────────────
  @Transactional
  public void subscribe(String endpoint, String p256dh, String auth) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepo.findByEmail(email).orElseThrow();

    subRepo.findByEndpoint(endpoint).ifPresentOrElse(
        existing -> { existing.setP256dh(p256dh); existing.setAuth(auth); subRepo.save(existing); },
        () -> subRepo.save(PushSubscription.builder()
            .user(user).endpoint(endpoint).p256dh(p256dh).auth(auth).build())
    );
    log.info("Push subscription saved for user {}", email);
  }

  @Transactional
  public void unsubscribe(String endpoint) {
    subRepo.deleteByEndpoint(endpoint);
  }

  // ── Scheduled: check overdue actions every 30 min ─────────────
  @Scheduled(fixedDelay = 1800000, initialDelay = 60000)
  @Transactional(readOnly = true)
  public void checkOverdueActions() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    OffsetDateTime oneHourAgo = now.minusHours(1);

    // Find actions that became overdue in the last 30 min (to avoid repeat spam)
    List<Action> overdue = actionRepo.findByStatusAndScheduledAtBetween(
        ActionStatus.PENDING, oneHourAgo, now);

    for (Action action : overdue) {
      User responsible = action.getResponsibleUser();
      if (responsible == null) continue;

      List<PushSubscription> subs = subRepo.findByUserId(responsible.getId());
      for (PushSubscription sub : subs) {
        sendNotification(sub,
            "⏰ Acción vencida",
            action.getTitle() + " — " + action.getClient().getBusinessName());
      }
    }
  }

  // ── Send a single push notification (simple HTTP POST) ────────
  public void sendNotification(PushSubscription sub, String title, String body) {
    try {
      Map<String, Object> payload = Map.of(
          "title", title,
          "body", body,
          "icon", "/logo.png",
          "badge", "/logo.png",
          "tag", "crm-action"
      );
      String json = objectMapper.writeValueAsString(payload);

      // Simple Web Push without VAPID signing library
      // Use the endpoint directly (works for basic notifications)
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(sub.getEndpoint()))
          .header("Content-Type", "application/json")
          .header("TTL", "86400")
          .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 400) {
        log.warn("Push failed for endpoint {}: status {}", sub.getEndpoint(), response.statusCode());
        if (response.statusCode() == 410 || response.statusCode() == 404) {
          subRepo.deleteByEndpoint(sub.getEndpoint());
        }
      }
    } catch (Exception e) {
      log.warn("Push error: {}", e.getMessage());
    }
  }
}
