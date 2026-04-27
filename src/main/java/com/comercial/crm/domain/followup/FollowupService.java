package com.comercial.crm.domain.followup;

import com.comercial.crm.domain.action.Action;
import com.comercial.crm.domain.action.ActionRepository;
import com.comercial.crm.domain.action.ActionStatus;
import com.comercial.crm.domain.client.Client;
import com.comercial.crm.domain.client.ClientCommercialStatus;
import com.comercial.crm.domain.client.ClientRepository;
import com.comercial.crm.domain.contact.Contact;
import com.comercial.crm.domain.contact.ContactRepository;
import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.web.dto.followup.FollowupRequest;
import com.comercial.crm.web.dto.followup.FollowupResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowupService {

  private final FollowupRepository  followupRepository;
  private final ClientRepository    clientRepository;
  private final ContactRepository   contactRepository;
  private final UserRepository      userRepository;
  private final ActionRepository    actionRepository;

  // ------------------------------------------------------------------ READ

  public Page<FollowupResponse> findByClient(UUID clientId, Pageable pageable) {
    getClientOrThrow(clientId);
    return followupRepository.findByClientIdOrderByEventAtDesc(clientId, pageable)
        .map(FollowupResponse::from);
  }

  public FollowupResponse findById(UUID id) {
    return FollowupResponse.from(getOrThrow(id));
  }

  // ----------------------------------------------------------------- CREATE

  @Transactional
  public FollowupResponse create(UUID clientId, FollowupRequest req) {
    Client  client  = getClientOrThrow(clientId);
    User    user    = currentUser();
    Contact contact = req.contactId() != null ? getContactOrThrow(req.contactId()) : null;

    // 1. Sincronizar estado comercial del cliente con el resultado del seguimiento
    syncClientStatus(client, req.result());

    // 2. Crear la próxima acción si viene en el request
    Action nextAction = null;
    if (req.nextAction() != null) {
      nextAction = buildAndSaveNextAction(client, contact, req.nextAction());
    }

    // 3. Construir el seguimiento
    Followup followup = Followup.builder()
        .client(client)
        .contact(contact)
        .user(user)
        .type(req.type())
        .result(req.result())
        .notes(req.notes())
        .eventAt(req.eventAt() != null ? req.eventAt() : OffsetDateTime.now())
        .nextAction(nextAction)
        .build();

    return FollowupResponse.from(followupRepository.save(followup));
  }

  // ----------------------------------------------------------------- DELETE
  // Los seguimientos son inmutables una vez creados (son historial).
  // Solo ADMIN puede eliminar.

  @Transactional
  public void delete(UUID clientId, UUID followupId) {
    Followup followup = getOrThrow(followupId);
    if (!followup.getClient().getId().equals(clientId)) {
      throw new IllegalArgumentException("El seguimiento no pertenece al cliente indicado");
    }
    followupRepository.delete(followup);
  }

  // ------------------------------------------------------------ PRIVATE helpers

  /**
   * Mapea el resultado del seguimiento al estado comercial del cliente.
   * Mantiene coherencia sin que el usuario tenga que actualizar el estado a mano.
   */
  private void syncClientStatus(Client client, FollowupResult result) {
    ClientCommercialStatus newStatus = switch (result) {
      case INTERESTED   -> ClientCommercialStatus.INTERESTED;
      case NEGOTIATION  -> ClientCommercialStatus.NEGOTIATION;
      case PAUSED       -> ClientCommercialStatus.PAUSED;
      case WON          -> ClientCommercialStatus.WON;
      case LOST         -> ClientCommercialStatus.LOST;
      case NO_RESPONSE  -> client.getCommercialStatus(); // no cambia
    };
    client.setCommercialStatus(newStatus);
    clientRepository.save(client);
  }

  private Action buildAndSaveNextAction(
      Client client, Contact contact, FollowupRequest.NextActionRequest actionReq
  ) {
    User responsible = actionReq.responsibleUserId() != null
        ? getUserOrThrow(actionReq.responsibleUserId())
        : currentUser();

    Action action = Action.builder()
        .client(client)
        .contact(contact)
        .responsibleUser(responsible)
        .title(actionReq.title())
        .type(actionReq.type())
        .status(ActionStatus.PENDING)
        .scheduledAt(actionReq.scheduledAt())
        .build();

    return actionRepository.save(action);
  }

  private Followup getOrThrow(UUID id) {
    return followupRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Seguimiento no encontrado: " + id));
  }

  private Client getClientOrThrow(UUID id) {
    return clientRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + id));
  }

  private Contact getContactOrThrow(UUID id) {
    return contactRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + id));
  }

  private User getUserOrThrow(UUID id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + id));
  }

  private User currentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
  }
}
