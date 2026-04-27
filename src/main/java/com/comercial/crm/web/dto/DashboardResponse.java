package com.comercial.crm.web.dto;

public record DashboardResponse(
    long activeClients,
    long pendingFollowups,
    long contactsTodayCount,
    long clientsWithoutFollowup,
    java.util.List<RecentActionItem> recentPendingActions
) {
  public record RecentActionItem(
      java.util.UUID actionId,
      java.util.UUID clientId,
      String clientBusinessName,
      String title,
      com.comercial.crm.domain.action.ActionType type,
      java.time.OffsetDateTime scheduledAt
  ) {}
}
