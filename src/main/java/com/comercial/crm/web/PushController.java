package com.comercial.crm.web;

import com.comercial.crm.domain.push.PushService;
import com.comercial.crm.web.dto.PushSubscriptionRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/push")
@RequiredArgsConstructor
public class PushController {

  private final PushService pushService;

  /** GET /push/vapid-public-key — devuelve la clave pública VAPID */
  @GetMapping("/vapid-public-key")
  public ResponseEntity<Map<String, String>> getVapidPublicKey(
      @org.springframework.beans.factory.annotation.Value("${push.vapid.public-key}") String key) {
    return ResponseEntity.ok(Map.of("publicKey", key));
  }

  /** POST /push/subscribe */
  @PostMapping("/subscribe")
  public ResponseEntity<Void> subscribe(@RequestBody PushSubscriptionRequest req) {
    pushService.subscribe(req.endpoint(), req.p256dh(), req.auth());
    return ResponseEntity.ok().build();
  }

  /** DELETE /push/unsubscribe */
  @DeleteMapping("/unsubscribe")
  public ResponseEntity<Void> unsubscribe(@RequestBody Map<String, String> body) {
    pushService.unsubscribe(body.get("endpoint"));
    return ResponseEntity.ok().build();
  }
}
