package com.lordkay.dispatchhub.tenant;

import com.lordkay.dispatchhub.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenant_membership")
public class TenantMembership {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private TenantRole role;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected TenantMembership() {
	}

	public TenantMembership(UUID id, Tenant tenant, AppUser user, TenantRole role, Instant createdAt) {
		this.id = id;
		this.tenant = tenant;
		this.user = user;
		this.role = role;
		this.createdAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public AppUser getUser() {
		return user;
	}

	public TenantRole getRole() {
		return role;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
