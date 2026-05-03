package com.comercial.crm.domain.client;

import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.web.dto.client.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

  private final ClientRepository             clientRepository;
  private final UserRepository               userRepository;
  private final ClientStatusHistoryRepository historyRepository;

  // ------------------------------------------------------------------ READ

  public Page<ClientSummaryResponse> search(String query, ClientCommercialStatus status, UUID ownerId, Pageable pageable) {
    String q = StringUtils.hasText(query) ? query.trim() : null;
    return clientRepository.search(q, status, ownerId, pageable).map(ClientSummaryResponse::from);
  }

  public ClientResponse findById(UUID id) {
    return ClientResponse.from(getOrThrow(id));
  }

  public List<StatusHistoryResponse> getStatusHistory(UUID clientId) {
    getOrThrow(clientId);
    return historyRepository.findByClientIdOrderByChangedAtAsc(clientId)
        .stream().map(StatusHistoryResponse::from).toList();
  }

  // ----------------------------------------------------------------- CREATE

  @Transactional
  public ClientResponse create(ClientRequest req) {
    validateNitUniqueness(req.nit(), null);
    User owner = resolveOwner(req.ownerUserId());

    Client client = Client.builder()
        .businessName(req.businessName())
        .nit(req.nit())
        .mainEmail(req.mainEmail())
        .mainPhone(req.mainPhone())
        .address(req.address())
        .city(req.city())
        .commercialStatus(req.commercialStatus() != null ? req.commercialStatus() : ClientCommercialStatus.NEW)
        .ownerUser(owner)
        .build();

    Client saved = clientRepository.save(client);

    // Registrar estado inicial en historial
    saveHistory(saved, null, saved.getCommercialStatus(), owner);

    return ClientResponse.from(saved);
  }

  // ----------------------------------------------------------------- UPDATE

  @Transactional
  public ClientResponse update(UUID id, ClientRequest req) {
    Client client = getOrThrow(id);
    validateNitUniqueness(req.nit(), id);

    client.setBusinessName(req.businessName());
    client.setNit(req.nit());
    client.setMainEmail(req.mainEmail());
    client.setMainPhone(req.mainPhone());
    client.setAddress(req.address());
    client.setCity(req.city());

    if (req.commercialStatus() != null && req.commercialStatus() != client.getCommercialStatus()) {
      ClientCommercialStatus oldStatus = client.getCommercialStatus();
      client.setCommercialStatus(req.commercialStatus());
      saveHistory(client, oldStatus, req.commercialStatus(), currentUser());
    }
    if (req.ownerUserId() != null) {
      client.setOwnerUser(getUserOrThrow(req.ownerUserId()));
    }

    return ClientResponse.from(clientRepository.save(client));
  }

  // ---------------------------------------------------------- PATCH STATUS

  @Transactional
  public ClientResponse updateStatus(UUID id, ClientStatusRequest req) {
    Client client = getOrThrow(id);
    ClientCommercialStatus oldStatus = client.getCommercialStatus();

    if (oldStatus != req.status()) {
      client.setCommercialStatus(req.status());
      saveHistory(client, oldStatus, req.status(), currentUser());
    }

    return ClientResponse.from(clientRepository.save(client));
  }

  @Transactional
  public void delete(UUID id) {
    clientRepository.delete(getOrThrow(id));
  }

  private Client getOrThrow(UUID id) {
    return clientRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));
  }

  private User resolveOwner(UUID ownerUserId) {
    if (ownerUserId != null) return getUserOrThrow(ownerUserId);
    return currentUser();
  }

  private User currentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email).orElse(null);
  }

  private void saveHistory(Client client, ClientCommercialStatus from, ClientCommercialStatus to, User changer) {
    historyRepository.save(ClientStatusHistory.builder()
        .client(client)
        .fromStatus(from)
        .toStatus(to)
        .changedBy(changer)
        .changedAt(OffsetDateTime.now())
        .build());
  }

  private void validateNitUniqueness(String nit, UUID excludeId) {
    if (!StringUtils.hasText(nit)) return;
    boolean duplicate = excludeId == null
        ? clientRepository.existsByNit(nit)
        : clientRepository.existsByNitAndIdNot(nit, excludeId);
    if (duplicate) throw new IllegalArgumentException("Ya existe un cliente con el NIT: " + nit);
  }
}
