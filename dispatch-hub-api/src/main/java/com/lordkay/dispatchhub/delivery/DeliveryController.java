package com.lordkay.dispatchhub.delivery;

import com.lordkay.dispatchhub.delivery.dto.DeliveryAttemptResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/tenants/{tenantId}/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
public class DeliveryController {

	private final DeliveryQueryService deliveryQueryService;
	private final DeliveryCommandService deliveryCommandService;

	public DeliveryController(DeliveryQueryService deliveryQueryService,
			DeliveryCommandService deliveryCommandService) {
		this.deliveryQueryService = deliveryQueryService;
		this.deliveryCommandService = deliveryCommandService;
	}

	@GetMapping("/{jobId}/attempts")
	public List<DeliveryAttemptResponse> attempts(@PathVariable UUID tenantId, @PathVariable UUID jobId) {
		return deliveryQueryService.listAttempts(tenantId, jobId);
	}

	@PostMapping("/{jobId}/retry")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public void retry(@PathVariable UUID tenantId, @PathVariable UUID jobId) {
		deliveryCommandService.retry(tenantId, jobId);
	}
}
