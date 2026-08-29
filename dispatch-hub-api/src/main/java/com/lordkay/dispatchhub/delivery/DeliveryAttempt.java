package com.lordkay.dispatchhub.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_attempt")
public class DeliveryAttempt {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "job_id", nullable = false)
	private UUID jobId;

	@Column(name = "attempt_number", nullable = false)
	private int attemptNumber;

	@Column(name = "http_status")
	private Integer httpStatus;

	@Column(name = "duration_ms", nullable = false)
	private long durationMs;

	@Column(name = "error_message", length = 2000)
	private String errorMessage;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected DeliveryAttempt() {
	}

	public DeliveryAttempt(UUID id, UUID tenantId, UUID jobId, int attemptNumber, Integer httpStatus, long durationMs,
			String errorMessage, Instant createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.jobId = jobId;
		this.attemptNumber = attemptNumber;
		this.httpStatus = httpStatus;
		this.durationMs = durationMs;
		this.errorMessage = errorMessage;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getJobId() {
		return jobId;
	}

	public int getAttemptNumber() {
		return attemptNumber;
	}

	public Integer getHttpStatus() {
		return httpStatus;
	}

	public long getDurationMs() {
		return durationMs;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
