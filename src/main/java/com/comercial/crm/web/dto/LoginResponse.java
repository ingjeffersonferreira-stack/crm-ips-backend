package com.comercial.crm.web.dto;

import java.util.List;

public record LoginResponse(
    String token,
    String email,
    List<String> roles
) {}