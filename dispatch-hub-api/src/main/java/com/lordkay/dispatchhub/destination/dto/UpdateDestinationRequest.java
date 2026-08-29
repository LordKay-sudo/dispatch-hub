package com.lordkay.dispatchhub.destination.dto;

import jakarta.validation.constraints.Size;

public record UpdateDestinationRequest(
		@Size(max = 128) String name,
		@Size(max = 2048) String targetUrl,
		@Size(max = 512) String secret,
		Boolean enabled) {
}
