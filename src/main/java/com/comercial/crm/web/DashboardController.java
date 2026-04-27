package com.comercial.crm.web;

import com.comercial.crm.domain.action.Action;
import com.comercial.crm.domain.action.ActionRepository;
import com.comercial.crm.domain.action.ActionStatus;
import com.comercial.crm.domain.client.ClientRepository;
import com.comercial.crm.domain.followup.FollowupRepository;
import com.comercial.crm.web.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ClientRepository clientRepository;
    private final ActionRepository actionRepository;
    private final FollowupRepository followupRepository;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<DashboardResponse> get() {

        long activeClients = clientRepository.countActive();
        long pendingFollowups = actionRepository.countByStatus(ActionStatus.PENDING);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        OffsetDateTime startOfDay = today.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime endOfDay = startOfDay.plusDays(1);
        long contactsToday = followupRepository.countTodayFollowups(startOfDay, endOfDay);

        OffsetDateTime since14Days = OffsetDateTime.now(ZoneOffset.UTC).minusDays(14);
        Set<java.util.UUID> recentlyContactedIds = new java.util.HashSet<>(
                followupRepository.findClientIdsWithRecentFollowup(since14Days));
        Set<java.util.UUID> withPendingActionIds = new java.util.HashSet<>(
                actionRepository.findClientIdsWithPendingActions());
        recentlyContactedIds.addAll(withPendingActionIds);

        long totalClients = clientRepository.count();
        long clientsWithoutFollowup = Math.max(0, totalClients - recentlyContactedIds.size());

        List<Action> upcomingActions = actionRepository.findPendingFrom(
                OffsetDateTime.now(ZoneOffset.UTC),
                PageRequest.of(0, 5));

        List<DashboardResponse.RecentActionItem> recentItems = upcomingActions.stream()
                .map(a -> new DashboardResponse.RecentActionItem(
                        a.getId(),
                        a.getClient().getId(),
                        a.getClient().getBusinessName(),
                        a.getTitle(),
                        a.getType(),
                        a.getScheduledAt()))
                .toList();

        return ResponseEntity.ok(new DashboardResponse(
                activeClients,
                pendingFollowups,
                contactsToday,
                clientsWithoutFollowup,
                recentItems));
    }
}