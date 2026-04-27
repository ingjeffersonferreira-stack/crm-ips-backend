package com.comercial.crm.web;

import com.comercial.crm.domain.client.ClientCommercialStatus;
import com.comercial.crm.domain.client.ClientService;
import com.comercial.crm.web.dto.client.ClientRequest;
import com.comercial.crm.web.dto.client.ClientResponse;
import com.comercial.crm.web.dto.client.ClientStatusRequest;
import com.comercial.crm.web.dto.client.ClientSummaryResponse;
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
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

  private final ClientService clientService;

  /**
   * GET /clients?query=lab&status=INTERESTED&page=0&size=20&sort=businessName,asc
   * Accessible by ADMIN and SALES roles.
   */
  @GetMapping
  public ResponseEntity<Page<ClientSummaryResponse>> list(
      @RequestParam(required = false) String query,
      @RequestParam(required = false) ClientCommercialStatus status,
      @PageableDefault(size = 20, sort = "businessName", direction = Sort.Direction.ASC) Pageable pageable
  ) {
    return ResponseEntity.ok(clientService.search(query, status, pageable));
  }

  /**
   * GET /clients/{id}
   */
  @GetMapping("/{id}")
  public ResponseEntity<ClientResponse> getById(@PathVariable UUID id) {
    return ResponseEntity.ok(clientService.findById(id));
  }

  /**
   * POST /clients
   */
  @PostMapping
  public ResponseEntity<ClientResponse> create(@Valid @RequestBody ClientRequest req) {
    ClientResponse created = clientService.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  /**
   * PUT /clients/{id}  — full update
   */
  @PutMapping("/{id}")
  public ResponseEntity<ClientResponse> update(
      @PathVariable UUID id,
      @Valid @RequestBody ClientRequest req
  ) {
    return ResponseEntity.ok(clientService.update(id, req));
  }

  /**
   * PATCH /clients/{id}/status  — change commercial status only
   */
  @PatchMapping("/{id}/status")
  public ResponseEntity<ClientResponse> updateStatus(
      @PathVariable UUID id,
      @Valid @RequestBody ClientStatusRequest req
  ) {
    return ResponseEntity.ok(clientService.updateStatus(id, req));
  }

  /**
   * DELETE /clients/{id}  — restricted to ADMIN only
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    clientService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
