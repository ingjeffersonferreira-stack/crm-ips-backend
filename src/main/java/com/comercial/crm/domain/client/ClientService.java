package com.comercial.crm.domain.client;

import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.web.dto.client.ClientRequest;
import com.comercial.crm.web.dto.client.ClientResponse;
import com.comercial.crm.web.dto.client.ClientStatusRequest;
import com.comercial.crm.web.dto.client.ClientSummaryResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

  private final ClientRepository clientRepository;
  private final UserRepository userRepository;

  // ------------------------------------------------------------------ READ

  public Page<ClientSummaryResponse> search(String query, ClientCommercialStatus status, Pageable pageable) {
    String q = StringUtils.hasText(query) ? query.trim() : null;
    return clientRepository.search(q, status, pageable)
        .map(ClientSummaryResponse::from);
  }

  public ClientResponse findById(UUID id) {
    return ClientResponse.from(getOrThrow(id));
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
        .commercialStatus(req.commercialStatus() != null
            ? req.commercialStatus()
            : ClientCommercialStatus.NEW)
        .ownerUser(owner)
        .build();

    return ClientResponse.from(clientRepository.save(client));
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

    if (req.commercialStatus() != null) {
      client.setCommercialStatus(req.commercialStatus());
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
    client.setCommercialStatus(req.status());
    return ClientResponse.from(clientRepository.save(client));
  }

  // ----------------------------------------------------------------- DELETE

  @Transactional
  public void delete(UUID id) {
    Client client = getOrThrow(id);
    clientRepository.delete(client);
  }

  // ------------------------------------------------------------ PRIVATE helpers

  private Client getOrThrow(UUID id) {
    return clientRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));
  }

  private User getUserOrThrow(UUID userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));
  }

  /**
   * Resolves the owner: if ownerUserId is provided, use that user;
   * otherwise fall back to the currently authenticated user.
   */
  private User resolveOwner(UUID ownerUserId) {
    if (ownerUserId != null) {
      return getUserOrThrow(ownerUserId);
    }
    String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(currentEmail).orElse(null);
  }

  private void validateNitUniqueness(String nit, UUID excludeId) {
    if (!StringUtils.hasText(nit)) return;
    boolean duplicate = excludeId == null
        ? clientRepository.existsByNit(nit)
        : clientRepository.existsByNitAndIdNot(nit, excludeId);
    if (duplicate) {
      throw new IllegalArgumentException("Ya existe un cliente con el NIT: " + nit);
    }
  }
}
