package com.comercial.crm.web;

import com.comercial.crm.domain.contact.ContactService;
import com.comercial.crm.web.dto.contact.ContactRequest;
import com.comercial.crm.web.dto.contact.ContactResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/clients/{clientId}/contacts")
@RequiredArgsConstructor
public class ContactController {

  private final ContactService contactService;

  /** GET /clients/{clientId}/contacts */
  @GetMapping
  public ResponseEntity<List<ContactResponse>> list(@PathVariable UUID clientId) {
    return ResponseEntity.ok(contactService.findByClient(clientId));
  }

  /** GET /clients/{clientId}/contacts/{contactId} */
  @GetMapping("/{contactId}")
  public ResponseEntity<ContactResponse> getById(
      @PathVariable UUID clientId,
      @PathVariable UUID contactId
  ) {
    return ResponseEntity.ok(contactService.findById(contactId));
  }

  /** POST /clients/{clientId}/contacts */
  @PostMapping
  public ResponseEntity<ContactResponse> create(
      @PathVariable UUID clientId,
      @Valid @RequestBody ContactRequest req
  ) {
    return ResponseEntity.status(HttpStatus.CREATED).body(contactService.create(clientId, req));
  }

  /** PUT /clients/{clientId}/contacts/{contactId} */
  @PutMapping("/{contactId}")
  public ResponseEntity<ContactResponse> update(
      @PathVariable UUID clientId,
      @PathVariable UUID contactId,
      @Valid @RequestBody ContactRequest req
  ) {
    return ResponseEntity.ok(contactService.update(clientId, contactId, req));
  }

  /** DELETE /clients/{clientId}/contacts/{contactId} */
  @DeleteMapping("/{contactId}")
  public ResponseEntity<Void> delete(
      @PathVariable UUID clientId,
      @PathVariable UUID contactId
  ) {
    contactService.delete(clientId, contactId);
    return ResponseEntity.noContent().build();
  }
}
