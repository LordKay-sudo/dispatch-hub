package com.lordkay.dispatchhub.event;

import com.lordkay.dispatchhub.common.ApiException;
import com.lordkay.dispatchhub.delivery.DeliveryJob;
import com.lordkay.dispatchhub.delivery.DeliveryJobRepository;
import com.lordkay.dispatchhub.delivery.DeliveryJobStatus;
import com.lordkay.dispatchhub.destination.Destination;
import com.lordkay.dispatchhub.destination.DestinationRepository;
import com.lordkay.dispatchhub.event.dto.EventResponse;
import com.lordkay.dispatchhub.event.dto.SubmitEventRequest;
import com.lordkay.dispatchhub.security.TenantGuard;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class EventService {

	private final InboundEventRepository eventRepository;
	private final DeliveryJobRepository deliveryJobRepository;
	private final DestinationRepository destinationRepository;
	private final TenantGuard tenantGuard;
	private final ObjectMapper objectMapper;

	public EventService(InboundEventRepository eventRepository, DeliveryJobRepository deliveryJobRepository,
			DestinationRepository destinationRepository, TenantGuard tenantGuard, ObjectMapper objectMapper) {
		this.eventRepository = eventRepository;
		this.deliveryJobRepository = deliveryJobRepository;
		this.destinationRepository = destinationRepository;
		this.tenantGuard = tenantGuard;
		this.objectMapper = objectMapper;
	}

	@Transactional
	public EventResponse submit(UUID tenantId, SubmitEventRequest request) {
		tenantGuard.requireTenantId(tenantId);
		tenantGuard.requireAdmin();

		String key = request.idempotencyKey().trim();
		var existing = eventRepository.findByTenantIdAndIdempotencyKey(tenantId, key);
		if (existing.isPresent()) {
			return toResponse(existing.get());
		}

		List<Destination> destinations = resolveDestinations(tenantId, request.destinationId());
		if (destinations.isEmpty()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "No enabled destinations to dispatch");
		}

		String payloadJson = toJson(request.payload());
		Instant now = Instant.now();
		InboundEvent event = new InboundEvent(UUID.randomUUID(), tenantId, key, payloadJson, now);

		try {
			eventRepository.saveAndFlush(event);
		}
		catch (DataIntegrityViolationException ex) {
			return eventRepository.findByTenantIdAndIdempotencyKey(tenantId, key)
				.map(this::toResponse)
				.orElseThrow(() -> ex);
		}

		List<DeliveryJob> jobs = new ArrayList<>();
		for (Destination destination : destinations) {
			DeliveryJob job = new DeliveryJob(UUID.randomUUID(), tenantId, event.getId(), destination.getId(),
					DeliveryJobStatus.PENDING, 0, now, now, now);
			jobs.add(job);
		}
		deliveryJobRepository.saveAll(jobs);
		return toResponse(event);
	}

	@Transactional(readOnly = true)
	public List<EventResponse> list(UUID tenantId) {
		tenantGuard.requireTenantId(tenantId);
		return eventRepository.findByTenantIdOrderByCreatedAtDesc(tenantId).stream().map(this::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public EventResponse get(UUID tenantId, UUID eventId) {
		tenantGuard.requireTenantId(tenantId);
		InboundEvent event = eventRepository.findByIdAndTenantId(eventId, tenantId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
		return toResponse(event);
	}

	private List<Destination> resolveDestinations(UUID tenantId, UUID destinationId) {
		if (destinationId != null) {
			Destination destination = destinationRepository.findByIdAndTenantId(destinationId, tenantId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Destination not found"));
			if (!destination.isEnabled()) {
				throw new ApiException(HttpStatus.BAD_REQUEST, "Destination is disabled");
			}
			return List.of(destination);
		}
		return destinationRepository.findByTenantIdAndEnabledTrue(tenantId);
	}

	private EventResponse toResponse(InboundEvent event) {
		List<EventResponse.JobSummary> jobs = deliveryJobRepository
			.findByTenantIdAndEventIdOrderByCreatedAtAsc(event.getTenantId(), event.getId())
			.stream()
			.map(job -> new EventResponse.JobSummary(job.getId(), job.getDestinationId(), job.getStatus().name(),
					job.getAttemptCount()))
			.toList();
		return new EventResponse(event.getId(), event.getIdempotencyKey(), event.getPayload(), event.getCreatedAt(),
				jobs);
	}

	private String toJson(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		}
		catch (RuntimeException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "payload must be JSON-serializable");
		}
	}
}
