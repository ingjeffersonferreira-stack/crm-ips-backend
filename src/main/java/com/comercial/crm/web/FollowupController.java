package com.comercial.crm.web;

import com.comercial.crm.domain.followup.FollowupService;
import com.comercial.crm.web.dto.followup.FollowupRequest;
import com.comercial.crm.web.dto.followup.FollowupResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clients/{clientId}/followups")
@RequiredArgsConstructor
public class FollowupController {

  private final FollowupService followupService;

  /**
   * GET /clients/{clientId}/followups?page=0&size=10
   * Historial completo del cliente, más reciente primero.
   */
  @GetMapping
  public ResponseEntity<Page<FollowupResponse>> list(
      @PathVariable UUID clientId,
      @PageableDefault(size = 10, sort = "eventAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return ResponseEntity.ok(followupService.findByClient(clientId, pageable));
  }

  /**
   * GET /clients/{clientId}/followups/{followupId}
   */
  @GetMapping("/{followupId}")
  public ResponseEntity<FollowupResponse> getById(
      @PathVariable UUID clientId,
      @PathVariable UUID followupId
  ) {
    return ResponseEntity.ok(followupService.findById(followupId));
  }

  /**
   * POST /clients/{clientId}/followups
   * Registra un seguimiento y opcionalmente crea la próxima acción.
   * También sincroniza el estado comercial del cliente automáticamente.
   */
  @PostMapping
  public ResponseEntity<FollowupResponse> create(
      @PathVariable UUID clientId,
      @Valid @RequestBody FollowupRequest req
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(followupService.create(clientId, req));
  }

  /**
   * DELETE /clients/{clientId}/followups/{followupId}
   * Solo ADMIN — los seguimientos son historial inmutable.
   */
  @DeleteMapping("/{followupId}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Void> delete(
      @PathVariable UUID clientId,
      @PathVariable UUID followupId
  ) {
    followupService.delete(clientId, followupId);
    return ResponseEntity.noContent().build();
  }
}
