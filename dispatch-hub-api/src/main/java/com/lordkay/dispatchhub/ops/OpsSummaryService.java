package com.lordkay.dispatchhub.ops;

import com.lordkay.dispatchhub.delivery.DeliveryJobRepository;
import com.lordkay.dispatchhub.delivery.DeliveryJobStatus;
import com.lordkay.dispatchhub.ops.dto.OpsSummaryResponse;
import com.lordkay.dispatchhub.security.TenantGuard;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OpsSummaryService {

	private final DeliveryJobRepository deliveryJobRepository;
	private final TenantGuard tenantGuard;

	public OpsSummaryService(DeliveryJobRepository deliveryJobRepository, TenantGuard tenantGuard) {
		this.deliveryJobRepository = deliveryJobRepository;
		this.tenantGuard = tenantGuard;
	}

	@Transactional(readOnly = true)
	public OpsSummaryResponse summarize(UUID tenantId) {
		tenantGuard.requireTenantId(tenantId);
		return new OpsSummaryResponse(deliveryJobRepository.countByTenantId(tenantId),
				deliveryJobRepository.countByTenantIdAndStatus(tenantId, DeliveryJobStatus.PENDING),
				deliveryJobRepository.countByTenantIdAndStatus(tenantId, DeliveryJobStatus.RUNNING),
				deliveryJobRepository.countByTenantIdAndStatus(tenantId, DeliveryJobStatus.SUCCESS),
				deliveryJobRepository.countByTenantIdAndStatus(tenantId, DeliveryJobStatus.FAILED),
				deliveryJobRepository.countByTenantIdAndStatus(tenantId, DeliveryJobStatus.DEAD));
	}
}
