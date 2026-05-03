package com.comercial.crm.web;

import com.comercial.crm.domain.client.ClientCommercialStatus;
import com.comercial.crm.domain.client.ClientService;
import com.comercial.crm.web.dto.client.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  /** GET /clients?query=lab&status=INTERESTED&ownerId=uuid&page=0&size=20 */
  @GetMapping
  public ResponseEntity<Page<ClientSummaryResponse>> list(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) ClientCommercialStatus status,
      @RequestParam(required = false) UUID ownerId,
      @PageableDefault(size = 20, sort = "businessName", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    return ResponseEntity.ok(clientService.search(query, status, ownerId, pageable));
  }

  /** GET /clients/{id} */
  @GetMapping("/{id}")
  public ResponseEntity<ClientResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(clientService.findById(id));
  }

  /** POST /clients */
  @PostMapping
  public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(req));
  }

  /** PUT /clients/{id} */
  @PutMapping("/{id}")
  public ResponseEntity<ClientResponse> update(@PathVariable UUID id, @Valid @RequestBody ClientRequest req) {
    return ResponseEntity.ok(clientService.update(id, req));
  }

  /** PATCH /clients/{id}/status */
  @PatchMapping("/{id}/status")
  public ResponseEntity<ClientResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody ClientStatusRequest req) {
    return ResponseEntity.ok(clientService.updateStatus(id, req));
  }

  /** GET /clients/{id}/status-history */
  @GetMapping("/{id}/status-history")
  @Transactional(readOnly = true)
  public ResponseEntity<List<StatusHistoryResponse>> getStatusHistory(@PathVariable UUID id) {
    return ResponseEntity.ok(clientService.getStatusHistory(id));
  }

  /** DELETE /clients/{id} — ADMIN only */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    clientService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
