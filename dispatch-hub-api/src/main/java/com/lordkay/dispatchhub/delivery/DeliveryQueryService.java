package com.lordkay.dispatchhub.delivery;

import com.lordkay.dispatchhub.common.ApiException;
import com.lordkay.dispatchhub.delivery.dto.DeliveryAttemptResponse;
import com.lordkay.dispatchhub.security.TenantGuard;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryQueryService {

	private final DeliveryJobRepository deliveryJobRepository;
	private final DeliveryAttemptRepository deliveryAttemptRepository;
	private final TenantGuard tenantGuard;

	public DeliveryQueryService(DeliveryJobRepository deliveryJobRepository,
			DeliveryAttemptRepository deliveryAttemptRepository, TenantGuard tenantGuard) {
		this.deliveryJobRepository = deliveryJobRepository;
		this.deliveryAttemptRepository = deliveryAttemptRepository;
		this.tenantGuard = tenantGuard;
	}

	@Transactional(readOnly = true)
	public List<DeliveryAttemptResponse> listAttempts(UUID tenantId, UUID jobId) {
		tenantGuard.requireTenantId(tenantId);
		deliveryJobRepository.findByIdAndTenantId(jobId, tenantId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery job not found"));
		return deliveryAttemptRepository.findByTenantIdAndJobIdOrderByAttemptNumberAsc(tenantId, jobId)
			.stream()
			.map(a -> new DeliveryAttemptResponse(a.getId(), a.getAttemptNumber(), a.getHttpStatus(), a.getDurationMs(),
					a.getErrorMessage(), a.getCreatedAt()))
			.toList();
	}
}
