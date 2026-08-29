package com.lordkay.dispatchhub.destination;

import com.lordkay.dispatchhub.destination.dto.CreateDestinationRequest;
import com.lordkay.dispatchhub.destination.dto.DestinationResponse;
import com.lordkay.dispatchhub.destination.dto.UpdateDestinationRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/tenants/{tenantId}/destinations", produces = MediaType.APPLICATION_JSON_VALUE)
public class DestinationController {

	private final DestinationService destinationService;

	public DestinationController(DestinationService destinationService) {
		this.destinationService = destinationService;
	}

	@GetMapping
	public List<DestinationResponse> list(@PathVariable UUID tenantId) {
		return destinationService.list(tenantId);
	}

	@GetMapping("/{destinationId}")
	public DestinationResponse get(@PathVariable UUID tenantId, @PathVariable UUID destinationId) {
		return destinationService.get(tenantId, destinationId);
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public DestinationResponse create(@PathVariable UUID tenantId, @Valid @RequestBody CreateDestinationRequest request) {
		return destinationService.create(tenantId, request);
	}

	@PutMapping(path = "/{destinationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public DestinationResponse update(@PathVariable UUID tenantId, @PathVariable UUID destinationId,
			@Valid @RequestBody UpdateDestinationRequest request) {
		return destinationService.update(tenantId, destinationId, request);
	}

	@DeleteMapping("/{destinationId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable UUID tenantId, @PathVariable UUID destinationId) {
		destinationService.delete(tenantId, destinationId);
	}
}
