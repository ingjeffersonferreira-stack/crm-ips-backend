package com.comercial.crm.domain.action;

import com.comercial.crm.domain.client.Client;
import com.comercial.crm.domain.client.ClientRepository;
import com.comercial.crm.domain.contact.Contact;
import com.comercial.crm.domain.contact.ContactRepository;
import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.web.dto.action.ActionRequest;
import com.comercial.crm.web.dto.action.ActionResponse;
import com.comercial.crm.web.dto.action.ActionStatusRequest;
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
public class ActionService {

  private final ActionRepository actionRepository;
  private final ClientRepository clientRepository;
  private final ContactRepository contactRepository;
  private final UserRepository userRepository;

  public Page<ActionResponse> findByClient(UUID clientId, Pageable pageable) {
    return actionRepository.findByClientId(clientId, pageable).map(ActionResponse::from);
  }

  public ActionResponse findById(UUID id) {
    return ActionResponse.from(getOrThrow(id));
  }

  @Transactional
  public ActionResponse create(UUID clientId, ActionRequest req) {
    Client client = getClientOrThrow(clientId);
    User responsible = resolveResponsible(req.responsibleUserId());
    Contact contact = req.contactId() != null ? getContactOrThrow(req.contactId()) : null;

    Action action = Action.builder()
        .client(client)
        .contact(contact)
        .responsibleUser(responsible)
        .title(req.title())
        .type(req.type())
        .scheduledAt(req.scheduledAt())
        .build();

    return ActionResponse.from(actionRepository.save(action));
  }

  @Transactional
  public ActionResponse update(UUID clientId, UUID actionId, ActionRequest req) {
    Action action = getOrThrow(actionId);
    validateBelongsToClient(action, clientId);

    Contact contact = req.contactId() != null ? getContactOrThrow(req.contactId()) : null;
    User responsible = resolveResponsible(req.responsibleUserId());

    action.setTitle(req.title());
    action.setType(req.type());
    action.setScheduledAt(req.scheduledAt());
    action.setContact(contact);
    action.setResponsibleUser(responsible);

    return ActionResponse.from(actionRepository.save(action));
  }

  @Transactional
  public ActionResponse updateStatus(UUID clientId, UUID actionId, ActionStatusRequest req) {
    Action action = getOrThrow(actionId);
    validateBelongsToClient(action, clientId);

    action.setStatus(req.status());
    if (req.status() == ActionStatus.COMPLETED) {
      action.setCompletedAt(OffsetDateTime.now());
    } else {
      action.setCompletedAt(null);
    }

    return ActionResponse.from(actionRepository.save(action));
  }

  @Transactional
  public void delete(UUID clientId, UUID actionId) {
    Action action = getOrThrow(actionId);
    validateBelongsToClient(action, clientId);
    actionRepository.delete(action);
  }

  // ---- helpers

  private Action getOrThrow(UUID id) {
    return actionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Acción no encontrada: " + id));
  }

  private Client getClientOrThrow(UUID clientId) {
    return clientRepository.findById(clientId)
        .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clientId));
  }

  private Contact getContactOrThrow(UUID contactId) {
    return contactRepository.findById(contactId)
        .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + contactId));
  }

  private User resolveResponsible(UUID userId) {
    if (userId != null) {
      return userRepository.findById(userId)
          .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado: " + userId));
    }
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new EntityNotFoundException("Usuario autenticado no encontrado"));
  }

  private void validateBelongsToClient(Action action, UUID clientId) {
    if (!action.getClient().getId().equals(clientId)) {
      throw new IllegalArgumentException("La acción no pertenece al cliente indicado");
    }
  }
}
