package com.comercial.crm.web.dto;

import com.comercial.crm.web.dto.action.ActionResponse;

import java.util.List;

public record AgendaResponse(
    String userName,
    List<ActionResponse> overdue,
    List<ActionResponse> today,
    List<ActionResponse> thisWeek,
    List<ActionResponse> later
) {}
