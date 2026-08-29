package com.lordkay.dispatchhub.delivery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_job")
public class DeliveryJob {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "event_id", nullable = false)
	private UUID eventId;

	@Column(name = "destination_id", nullable = false)
	private UUID destinationId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private DeliveryJobStatus status;

	@Column(name = "attempt_count", nullable = false)
	private int attemptCount;

	@Column(name = "next_run_at", nullable = false)
	private Instant nextRunAt;

	@Column(name = "last_error", length = 2000)
	private String lastError;

	@Column(name = "locked_at")
	private Instant lockedAt;

	@Column(name = "locked_by", length = 128)
	private String lockedBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected DeliveryJob() {
	}

	public DeliveryJob(UUID id, UUID tenantId, UUID eventId, UUID destinationId, DeliveryJobStatus status,
			int attemptCount, Instant nextRunAt, Instant createdAt, Instant updatedAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.eventId = eventId;
		this.destinationId = destinationId;
		this.status = status;
		this.attemptCount = attemptCount;
		this.nextRunAt = nextRunAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public UUID getEventId() {
		return eventId;
	}

	public UUID getDestinationId() {
		return destinationId;
	}

	public DeliveryJobStatus getStatus() {
		return status;
	}

	public void setStatus(DeliveryJobStatus status) {
		this.status = status;
	}

	public int getAttemptCount() {
		return attemptCount;
	}

	public void setAttemptCount(int attemptCount) {
		this.attemptCount = attemptCount;
	}

	public Instant getNextRunAt() {
		return nextRunAt;
	}

	public void setNextRunAt(Instant nextRunAt) {
		this.nextRunAt = nextRunAt;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public Instant getLockedAt() {
		return lockedAt;
	}

	public void setLockedAt(Instant lockedAt) {
		this.lockedAt = lockedAt;
	}

	public String getLockedBy() {
		return lockedBy;
	}

	public void setLockedBy(String lockedBy) {
		this.lockedBy = lockedBy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
