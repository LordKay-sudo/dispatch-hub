package com.lordkay.dispatchhub.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_egress_host")
public class TenantEgressHost {

	@Id
	private UUID id;

	@Column(name = "tenant_id", nullable = false)
	private UUID tenantId;

	@Column(name = "host_pattern", nullable = false)
	private String hostPattern;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected TenantEgressHost() {
	}

	public UUID getId() {
		return id;
	}

	public UUID getTenantId() {
		return tenantId;
	}

	public String getHostPattern() {
		return hostPattern;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
