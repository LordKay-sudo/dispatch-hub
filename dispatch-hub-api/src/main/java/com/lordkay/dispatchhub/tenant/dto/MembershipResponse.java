package com.lordkay.dispatchhub.tenant.dto;

import java.util.UUID;

public record MembershipResponse(UUID tenantId, String tenantCode, String role) {
}
