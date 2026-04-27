package com.comercial.crm.web;

import com.comercial.crm.domain.action.ActionService;
import com.comercial.crm.web.dto.action.ActionRequest;
import com.comercial.crm.web.dto.action.ActionResponse;
import com.comercial.crm.web.dto.action.ActionStatusRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clients/{clientId}/actions")
@RequiredArgsConstructor
public class ActionController {

  private final ActionService actionService;

  /** GET /clients/{clientId}/actions?page=0&size=10&sort=scheduledAt,asc */
  @GetMapping
  public ResponseEntity<Page<ActionResponse>> list(
      @PathVariable UUID clientId,
      @PageableDefault(size = 10, sort = "scheduledAt", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    return ResponseEntity.ok(actionService.findByClient(clientId, pageable));
  }

  /** GET /clients/{clientId}/actions/{actionId} */
  @GetMapping("/{actionId}")
  public ResponseEntity<ActionResponse> getById(
      @PathVariable UUID clientId,
      @PathVariable UUID actionId
  ) {
    return ResponseEntity.ok(actionService.findById(actionId));
  }

  /** POST /clients/{clientId}/actions */
  @PostMapping
  public ResponseEntity<ActionResponse> create(
      @PathVariable UUID clientId,
      @Valid @RequestBody ActionRequest req
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(actionService.create(clientId, req));
  }

  /** PUT /clients/{clientId}/actions/{actionId} */
  @PutMapping("/{actionId}")
  public ResponseEntity<ActionResponse> update(
      @PathVariable UUID clientId,
      @PathVariable UUID actionId,
      @Valid @RequestBody ActionRequest req
  ) {
    return ResponseEntity.ok(actionService.update(clientId, actionId, req));
  }

  /** PATCH /clients/{clientId}/actions/{actionId}/status */
  @PatchMapping("/{actionId}/status")
  public ResponseEntity<ActionResponse> updateStatus(
      @PathVariable UUID clientId,
      @PathVariable UUID actionId,
      @Valid @RequestBody ActionStatusRequest req
  ) {
    return ResponseEntity.ok(actionService.updateStatus(clientId, actionId, req));
  }

  /** DELETE /clients/{clientId}/actions/{actionId} */
  @DeleteMapping("/{actionId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID clientId,
      @PathVariable UUID actionId
  ) {
    actionService.delete(clientId, actionId);
    return ResponseEntity.noContent().build();
  }
}
