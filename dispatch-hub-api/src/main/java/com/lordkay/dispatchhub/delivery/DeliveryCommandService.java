package com.lordkay.dispatchhub.delivery;

import com.lordkay.dispatchhub.common.ApiException;
import com.lordkay.dispatchhub.dispatch.OutboxClaimRepository;
import com.lordkay.dispatchhub.security.TenantGuard;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryCommandService {

	private final DeliveryJobRepository deliveryJobRepository;
	private final OutboxClaimRepository outboxClaimRepository;
	private final TenantGuard tenantGuard;

	public DeliveryCommandService(DeliveryJobRepository deliveryJobRepository,
			OutboxClaimRepository outboxClaimRepository, TenantGuard tenantGuard) {
		this.deliveryJobRepository = deliveryJobRepository;
		this.outboxClaimRepository = outboxClaimRepository;
		this.tenantGuard = tenantGuard;
	}

	@Transactional
	public void retry(UUID tenantId, UUID jobId) {
		tenantGuard.requireTenantId(tenantId);
		tenantGuard.requireAdmin();
		DeliveryJob job = deliveryJobRepository.findByIdAndTenantId(jobId, tenantId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery job not found"));
		if (job.getStatus() != DeliveryJobStatus.FAILED && job.getStatus() != DeliveryJobStatus.DEAD) {
			throw new ApiException(HttpStatus.CONFLICT, "Only FAILED or DEAD jobs can be retried");
		}
		outboxClaimRepository.requeue(jobId);
	}
}
