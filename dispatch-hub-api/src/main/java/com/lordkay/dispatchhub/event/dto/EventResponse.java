package com.lordkay.dispatchhub.event.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record EventResponse(
		UUID id,
		String idempotencyKey,
		String payload,
		Instant createdAt,
		List<JobSummary> jobs) {

	public record JobSummary(UUID id, UUID destinationId, String status, int attemptCount) {
	}
}
