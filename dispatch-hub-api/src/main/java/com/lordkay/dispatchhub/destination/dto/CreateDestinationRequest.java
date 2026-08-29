package com.lordkay.dispatchhub.destination.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDestinationRequest(
		@NotBlank @Size(max = 128) String name,
		@NotBlank @Size(max = 2048) String targetUrl,
		@Size(max = 512) String secret,
		Boolean enabled) {
}
