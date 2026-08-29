package com.lordkay.dispatchhub.dispatch;

import com.lordkay.dispatchhub.delivery.DeliveryJob;
import com.lordkay.dispatchhub.delivery.DeliveryJobRepository;
import com.lordkay.dispatchhub.destination.Destination;
import com.lordkay.dispatchhub.destination.DestinationRepository;
import com.lordkay.dispatchhub.event.InboundEvent;
import com.lordkay.dispatchhub.event.InboundEventRepository;
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

	public DeliveryProcessor(DeliveryJobRepository deliveryJobRepository, DestinationRepository destinationRepository,
			InboundEventRepository inboundEventRepository, OutboxClaimRepository outboxClaimRepository,
			WebhookClient webhookClient) {
		this.deliveryJobRepository = deliveryJobRepository;
		this.destinationRepository = destinationRepository;
		this.inboundEventRepository = inboundEventRepository;
		this.outboxClaimRepository = outboxClaimRepository;
		this.webhookClient = webhookClient;
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
			outboxClaimRepository.markFailed(jobId, job.getAttemptCount() + 1, "Missing destination or event");
			return;
		}

		int attemptNumber = job.getAttemptCount() + 1;
		WebhookSendResult result = webhookClient.send(destination.getTargetUrl(), event.getPayload(),
				destination.getSecret());
		outboxClaimRepository.insertAttempt(UUID.randomUUID(), job.getTenantId(), jobId, attemptNumber,
				result.httpStatus(), result.durationMs(), result.errorMessage());

		if (result.success()) {
			outboxClaimRepository.markSuccess(jobId, attemptNumber);
		}
		else {
			outboxClaimRepository.markFailed(jobId, attemptNumber,
					result.errorMessage() != null ? result.errorMessage() : "Delivery failed");
		}
	}
}
