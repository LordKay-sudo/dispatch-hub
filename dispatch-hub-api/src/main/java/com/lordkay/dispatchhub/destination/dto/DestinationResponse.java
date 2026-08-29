package com.lordkay.dispatchhub.destination.dto;

import java.util.UUID;

public record DestinationResponse(
		UUID id,
		String name,
		String targetUrl,
		boolean hasSecret,
		boolean enabled) {
}
