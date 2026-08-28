package com.lordkay.dispatchhub.tenant.dto;

import java.util.UUID;

public record TenantResponse(UUID id, String code, String name) {
}
