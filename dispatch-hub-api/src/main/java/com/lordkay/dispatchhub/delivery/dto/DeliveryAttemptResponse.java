package com.lordkay.dispatchhub.delivery.dto;

import java.time.Instant;
import java.util.UUID;

public record DeliveryAttemptResponse(
		UUID id,
		int attemptNumber,
		Integer httpStatus,
		long durationMs,
		String errorMessage,
		Instant createdAt) {
}
