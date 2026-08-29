package com.lordkay.dispatchhub.ai;

import com.lordkay.dispatchhub.common.ApiException;
import com.lordkay.dispatchhub.delivery.DeliveryAttempt;
import com.lordkay.dispatchhub.delivery.DeliveryAttemptRepository;
import com.lordkay.dispatchhub.delivery.DeliveryJob;
import com.lordkay.dispatchhub.delivery.DeliveryJobRepository;
import com.lordkay.dispatchhub.security.TenantGuard;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FailureSummaryService {

	private final DeliveryJobRepository deliveryJobRepository;
	private final DeliveryAttemptRepository deliveryAttemptRepository;
	private final FailureSummarizer failureSummarizer;
	private final TenantGuard tenantGuard;

	public FailureSummaryService(DeliveryJobRepository deliveryJobRepository,
			DeliveryAttemptRepository deliveryAttemptRepository, FailureSummarizer failureSummarizer,
			TenantGuard tenantGuard) {
		this.deliveryJobRepository = deliveryJobRepository;
		this.deliveryAttemptRepository = deliveryAttemptRepository;
		this.failureSummarizer = failureSummarizer;
		this.tenantGuard = tenantGuard;
	}

	@Transactional(readOnly = true)
	public FailureSummary summarizeJob(UUID tenantId, UUID jobId) {
		tenantGuard.requireTenantId(tenantId);
		DeliveryJob job = deliveryJobRepository.findByIdAndTenantId(jobId, tenantId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Delivery job not found"));

		List<DeliveryAttempt> attempts = deliveryAttemptRepository
			.findByTenantIdAndJobIdOrderByAttemptNumberAsc(tenantId, jobId);
		DeliveryAttempt latest = attempts.stream().max(Comparator.comparingInt(DeliveryAttempt::getAttemptNumber))
			.orElse(null);

		FailureContext context = new FailureContext(job.getStatus().name(),
				latest != null ? latest.getAttemptNumber() : job.getAttemptCount(),
				latest != null ? latest.getHttpStatus() : null,
				latest != null ? latest.getErrorMessage() : job.getLastError());
		return failureSummarizer.summarize(context);
	}
}
