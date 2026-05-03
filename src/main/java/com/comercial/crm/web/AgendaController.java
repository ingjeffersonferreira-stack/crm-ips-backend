package com.comercial.crm.web;

import com.comercial.crm.domain.action.Action;
import com.comercial.crm.domain.action.ActionRepository;
import com.comercial.crm.domain.action.ActionStatus;
import com.comercial.crm.domain.user.User;
import com.comercial.crm.domain.user.UserRepository;
import com.comercial.crm.web.dto.AgendaResponse;
import com.comercial.crm.web.dto.action.ActionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/my-agenda")
@RequiredArgsConstructor
public class AgendaController {

  private final ActionRepository actionRepository;
  private final UserRepository   userRepository;

  /**
   * GET /my-agenda
   * Acciones PENDING del usuario autenticado, agrupadas por urgencia.
   */
  @GetMapping
  @Transactional(readOnly = true)
  public ResponseEntity<AgendaResponse> getMyAgenda() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    List<Action> pending = actionRepository
        .findByResponsibleUserIdAndStatusOrderByScheduledAtAsc(user.getId(), ActionStatus.PENDING);

    LocalDate today   = LocalDate.now(ZoneOffset.UTC);
    LocalDate in7days = today.plusDays(7);

    List<ActionResponse> overdue   = pending.stream()
        .filter(a -> a.getScheduledAt().toLocalDate().isBefore(today))
        .map(ActionResponse::from).toList();

    List<ActionResponse> todayList = pending.stream()
        .filter(a -> a.getScheduledAt().toLocalDate().isEqual(today))
        .map(ActionResponse::from).toList();

    List<ActionResponse> thisWeek  = pending.stream()
        .filter(a -> {
          LocalDate d = a.getScheduledAt().toLocalDate();
          return d.isAfter(today) && !d.isAfter(in7days);
        })
        .map(ActionResponse::from).toList();

    List<ActionResponse> later     = pending.stream()
        .filter(a -> a.getScheduledAt().toLocalDate().isAfter(in7days))
        .map(ActionResponse::from).toList();

    return ResponseEntity.ok(new AgendaResponse(
        user.getFullName(), overdue, todayList, thisWeek, later));
  }
}
