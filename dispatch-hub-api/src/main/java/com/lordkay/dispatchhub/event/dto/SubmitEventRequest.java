package com.lordkay.dispatchhub.event.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.UUID;

public record SubmitEventRequest(
		@NotBlank @Size(max = 128) String idempotencyKey,
		@NotNull Map<String, Object> payload,
		UUID destinationId) {
}
