package com.comercial.crm.domain.contact;

import com.comercial.crm.domain.client.Client;
import com.comercial.crm.domain.client.ClientRepository;
import com.comercial.crm.web.dto.contact.ContactRequest;
import com.comercial.crm.web.dto.contact.ContactResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactService {

  private final ContactRepository contactRepository;
  private final ClientRepository clientRepository;

  public List<ContactResponse> findByClient(UUID clientId) {
    getClientOrThrow(clientId); // valida que el cliente exista
    return contactRepository.findByClientIdOrderByIsPrimaryDescFullNameAsc(clientId)
        .stream().map(ContactResponse::from).toList();
  }

  public ContactResponse findById(UUID id) {
    return ContactResponse.from(getOrThrow(id));
  }

  @Transactional
  public ContactResponse create(UUID clientId, ContactRequest req) {
    Client client = getClientOrThrow(clientId);

    // Si el nuevo va a ser principal, limpiamos el anterior
    if (req.isPrimary()) {
      contactRepository.clearPrimaryByClientId(clientId);
    }

    Contact contact = Contact.builder()
        .client(client)
        .fullName(req.fullName())
        .jobTitle(req.jobTitle())
        .email(req.email())
        .phone(req.phone())
        .isPrimary(req.isPrimary())
        .build();

    return ContactResponse.from(contactRepository.save(contact));
  }

  @Transactional
  public ContactResponse update(UUID clientId, UUID contactId, ContactRequest req) {
    Contact contact = getOrThrow(contactId);
    validateBelongsToClient(contact, clientId);

    if (req.isPrimary() && !contact.isPrimary()) {
      contactRepository.clearPrimaryByClientId(clientId);
    }

    contact.setFullName(req.fullName());
    contact.setJobTitle(req.jobTitle());
    contact.setEmail(req.email());
    contact.setPhone(req.phone());
    contact.setPrimary(req.isPrimary());

    return ContactResponse.from(contactRepository.save(contact));
  }

  @Transactional
  public void delete(UUID clientId, UUID contactId) {
    Contact contact = getOrThrow(contactId);
    validateBelongsToClient(contact, clientId);
    contactRepository.delete(contact);
  }

  // ---- helpers

  private Contact getOrThrow(UUID id) {
    return contactRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Contacto no encontrado: " + id));
  }

  private Client getClientOrThrow(UUID clientId) {
    return clientRepository.findById(clientId)
        .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado: " + clientId));
  }

  private void validateBelongsToClient(Contact contact, UUID clientId) {
    if (!contact.getClient().getId().equals(clientId)) {
      throw new IllegalArgumentException("El contacto no pertenece al cliente indicado");
    }
  }
}
