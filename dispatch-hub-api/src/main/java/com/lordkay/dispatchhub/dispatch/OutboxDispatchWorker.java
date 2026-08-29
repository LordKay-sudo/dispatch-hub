package com.lordkay.dispatchhub.dispatch;

import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxDispatchWorker {

	private static final Logger log = LoggerFactory.getLogger(OutboxDispatchWorker.class);

	private final OutboxClaimRepository outboxClaimRepository;
	private final DeliveryProcessor deliveryProcessor;
	private final String workerId;
	private final int batchSize;

	public OutboxDispatchWorker(OutboxClaimRepository outboxClaimRepository, DeliveryProcessor deliveryProcessor,
			@Value("${app.dispatch.worker-id:dispatch-hub-1}") String workerId,
			@Value("${app.dispatch.batch-size:10}") int batchSize) {
		this.outboxClaimRepository = outboxClaimRepository;
		this.deliveryProcessor = deliveryProcessor;
		this.workerId = workerId;
		this.batchSize = batchSize;
	}

	@Scheduled(fixedDelayString = "${app.dispatch.poll-interval-ms:2000}")
	public void poll() {
		List<UUID> claimed = outboxClaimRepository.claimPending(workerId, batchSize);
		for (UUID jobId : claimed) {
			try {
				deliveryProcessor.process(jobId);
			}
			catch (Exception ex) {
				log.warn("Failed processing delivery job {}", jobId, ex);
			}
		}
	}
}
