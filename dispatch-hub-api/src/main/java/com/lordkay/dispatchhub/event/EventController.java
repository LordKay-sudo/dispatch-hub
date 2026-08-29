package com.lordkay.dispatchhub.event;

import com.lordkay.dispatchhub.event.dto.EventResponse;
import com.lordkay.dispatchhub.event.dto.SubmitEventRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/tenants/{tenantId}/events", produces = MediaType.APPLICATION_JSON_VALUE)
public class EventController {

	private final EventService eventService;

	public EventController(EventService eventService) {
		this.eventService = eventService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.ACCEPTED)
	public EventResponse submit(@PathVariable UUID tenantId, @Valid @RequestBody SubmitEventRequest request) {
		return eventService.submit(tenantId, request);
	}

	@GetMapping
	public List<EventResponse> list(@PathVariable UUID tenantId) {
		return eventService.list(tenantId);
	}

	@GetMapping("/{eventId}")
	public EventResponse get(@PathVariable UUID tenantId, @PathVariable UUID eventId) {
		return eventService.get(tenantId, eventId);
	}
}
