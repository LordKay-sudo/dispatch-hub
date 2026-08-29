package com.lordkay.dispatchhub.destination;

import com.lordkay.dispatchhub.common.ApiException;
import com.lordkay.dispatchhub.destination.dto.CreateDestinationRequest;
import com.lordkay.dispatchhub.destination.dto.DestinationResponse;
import com.lordkay.dispatchhub.destination.dto.UpdateDestinationRequest;
import com.lordkay.dispatchhub.security.TenantGuard;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DestinationService {

	private final DestinationRepository destinationRepository;
	private final DestinationUrlValidator urlValidator;
	private final TenantGuard tenantGuard;

	public DestinationService(DestinationRepository destinationRepository, DestinationUrlValidator urlValidator,
			TenantGuard tenantGuard) {
		this.destinationRepository = destinationRepository;
		this.urlValidator = urlValidator;
		this.tenantGuard = tenantGuard;
	}

	@Transactional(readOnly = true)
	public List<DestinationResponse> list(UUID tenantId) {
		tenantGuard.requireTenantId(tenantId);
		return destinationRepository.findByTenantIdOrderByNameAsc(tenantId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public DestinationResponse get(UUID tenantId, UUID destinationId) {
		tenantGuard.requireTenantId(tenantId);
		return toResponse(requireDestination(tenantId, destinationId));
	}

	@Transactional
	public DestinationResponse create(UUID tenantId, CreateDestinationRequest request) {
		tenantGuard.requireTenantId(tenantId);
		tenantGuard.requireAdmin();
		String url = urlValidator.requireHttpUrl(request.targetUrl());
		if (destinationRepository.existsByTenantIdAndNameIgnoreCase(tenantId, request.name().trim())) {
			throw new ApiException(HttpStatus.CONFLICT, "Destination name already exists for this tenant");
		}
		Instant now = Instant.now();
		Destination destination = new Destination(UUID.randomUUID(), tenantId, request.name().trim(), url,
				blankToNull(request.secret()), request.enabled() == null || request.enabled(), now, now);
		return toResponse(destinationRepository.save(destination));
	}

	@Transactional
	public DestinationResponse update(UUID tenantId, UUID destinationId, UpdateDestinationRequest request) {
		tenantGuard.requireTenantId(tenantId);
		tenantGuard.requireAdmin();
		Destination destination = requireDestination(tenantId, destinationId);
		if (request.name() != null && !request.name().isBlank()) {
			destination.setName(request.name().trim());
		}
		if (request.targetUrl() != null && !request.targetUrl().isBlank()) {
			destination.setTargetUrl(urlValidator.requireHttpUrl(request.targetUrl()));
		}
		if (request.secret() != null) {
			destination.setSecret(blankToNull(request.secret()));
		}
		if (request.enabled() != null) {
			destination.setEnabled(request.enabled());
		}
		destination.setUpdatedAt(Instant.now());
		return toResponse(destinationRepository.save(destination));
	}

	@Transactional
	public void delete(UUID tenantId, UUID destinationId) {
		tenantGuard.requireTenantId(tenantId);
		tenantGuard.requireAdmin();
		Destination destination = requireDestination(tenantId, destinationId);
		destinationRepository.delete(destination);
	}

	private Destination requireDestination(UUID tenantId, UUID destinationId) {
		return destinationRepository.findByIdAndTenantId(destinationId, tenantId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Destination not found"));
	}

	private DestinationResponse toResponse(Destination destination) {
		boolean hasSecret = destination.getSecret() != null && !destination.getSecret().isBlank();
		return new DestinationResponse(destination.getId(), destination.getName(), destination.getTargetUrl(),
				hasSecret, destination.isEnabled());
	}

	private static String blankToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value;
	}
}
