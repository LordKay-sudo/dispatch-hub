package com.lordkay.dispatchhub.dispatch;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OutboxClaimRepositoryCustom {

	List<UUID> claimPending(String workerId, int batchSize);

	void markSuccess(UUID jobId, int attemptCount);

	void scheduleRetry(UUID jobId, int attemptCount, String error, Instant nextRunAt);

	void markDead(UUID jobId, int attemptCount, String error);

	void markFailed(UUID jobId, int attemptCount, String error);

	void requeue(UUID jobId);

	void insertAttempt(UUID attemptId, UUID tenantId, UUID jobId, int attemptNumber, Integer httpStatus,
			long durationMs, String errorMessage);
}
