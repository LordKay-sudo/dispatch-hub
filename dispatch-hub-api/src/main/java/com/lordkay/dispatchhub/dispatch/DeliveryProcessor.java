package com.lordkay.dispatchhub.dispatch;

import com.lordkay.dispatchhub.delivery.DeliveryJob;
import com.lordkay.dispatchhub.delivery.DeliveryJobRepository;
import com.lordkay.dispatchhub.destination.Destination;
import com.lordkay.dispatchhub.destination.DestinationRepository;
import com.lordkay.dispatchhub.destination.DestinationUrlValidator;
import com.lordkay.dispatchhub.event.InboundEvent;
import com.lordkay.dispatchhub.event.InboundEventRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryProcessor {

	private final DeliveryJobRepository deliveryJobRepository;
	private final DestinationRepository destinationRepository;
	private final InboundEventRepository inboundEventRepository;
	private final OutboxClaimRepository outboxClaimRepository;
	private final WebhookClient webhookClient;
	private final DestinationUrlValidator destinationUrlValidator;
	private final DestinationRateLimiter rateLimiter;
	private final RetryBackoff retryBackoff;
	private final DispatchMetrics dispatchMetrics;

	public DeliveryProcessor(DeliveryJobRepository deliveryJobRepository, DestinationRepository destinationRepository,
			InboundEventRepository inboundEventRepository, OutboxClaimRepository outboxClaimRepository,
			WebhookClient webhookClient, DestinationUrlValidator destinationUrlValidator,
			DestinationRateLimiter rateLimiter, RetryBackoff retryBackoff, DispatchMetrics dispatchMetrics) {
		this.deliveryJobRepository = deliveryJobRepository;
		this.destinationRepository = destinationRepository;
		this.inboundEventRepository = inboundEventRepository;
		this.outboxClaimRepository = outboxClaimRepository;
		this.webhookClient = webhookClient;
		this.destinationUrlValidator = destinationUrlValidator;
		this.rateLimiter = rateLimiter;
		this.retryBackoff = retryBackoff;
		this.dispatchMetrics = dispatchMetrics;
	}

	@Transactional
	public void process(UUID jobId) {
		DeliveryJob job = deliveryJobRepository.findById(jobId).orElse(null);
		if (job == null) {
			return;
		}
		Destination destination = destinationRepository.findById(job.getDestinationId()).orElse(null);
		InboundEvent event = inboundEventRepository.findById(job.getEventId()).orElse(null);
		if (destination == null || event == null) {
			outboxClaimRepository.markDead(jobId, job.getAttemptCount() + 1, "Missing destination or event");
			dispatchMetrics.dead();
			return;
		}

		try {
			destinationUrlValidator.requireAllowedUrl(job.getTenantId(), destination.getTargetUrl());
		}
		catch (RuntimeException ex) {
			outboxClaimRepository.markDead(jobId, job.getAttemptCount() + 1, "SSRF check failed: " + ex.getMessage());
			dispatchMetrics.dead();
			return;
		}

		if (!rateLimiter.tryAcquire(job.getTenantId(), destination.getId())) {
			outboxClaimRepository.scheduleRetry(jobId, job.getAttemptCount(), "Rate limited",
					Instant.now().plusSeconds(30));
			dispatchMetrics.rateLimited();
			return;
		}

		int attemptNumber = job.getAttemptCount() + 1;
		WebhookSendResult result = webhookClient.send(destination.getTargetUrl(), event.getPayload(),
				destination.getSecret());
		outboxClaimRepository.insertAttempt(UUID.randomUUID(), job.getTenantId(), jobId, attemptNumber,
				result.httpStatus(), result.durationMs(), result.errorMessage());

		if (result.success()) {
			outboxClaimRepository.markSuccess(jobId, attemptNumber);
			dispatchMetrics.success();
			return;
		}

		String error = result.errorMessage() != null ? result.errorMessage() : "Delivery failed";
		if (retryBackoff.shouldRetry(attemptNumber)) {
			Instant next = Instant.now().plus(retryBackoff.delayAfterAttempt(attemptNumber));
			outboxClaimRepository.scheduleRetry(jobId, attemptNumber, error, next);
			dispatchMetrics.retry();
		}
		else {
			outboxClaimRepository.markDead(jobId, attemptNumber, error);
			dispatchMetrics.dead();
		}
	}
}
