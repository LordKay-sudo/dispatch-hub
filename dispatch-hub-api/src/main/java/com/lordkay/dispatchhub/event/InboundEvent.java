package com.lordkay.dispatchhub.event;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "inbound_event")
public class InboundEvent {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "idempotency_key", nullable = false, length = 128)
	private String idempotencyKey;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private String payload;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected InboundEvent() {
	}

	public InboundEvent(UUID id, UUID tenantId, String idempotencyKey, String payload, Instant createdAt) {
		this.id = id;
		this.tenantId = tenantId;
		this.idempotencyKey = idempotencyKey;
		this.payload = payload;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public String getIdempotencyKey() {
		return idempotencyKey;
	}

	public String getPayload() {
		return payload;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
