package com.lordkay.dispatchhub.auth.dto;

import java.util.UUID;

public record LoginResponse(
		String accessToken,
		String tokenType,
		long expiresIn,
		String username,
		UUID tenantId,
		String tenantCode,
		String role) {
}
