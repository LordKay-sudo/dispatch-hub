package com.lordkay.dispatchhub.security;

import com.lordkay.dispatchhub.common.ApiException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class TenantGuard {

	private final CurrentUser currentUser;

	public TenantGuard(CurrentUser currentUser) {
		this.currentUser = currentUser;
	}

	public UUID requireTenantId(UUID pathTenantId) {
		if (!currentUser.tenantId().equals(pathTenantId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Token is not scoped to this tenant");
		}
		return pathTenantId;
	}

	public void requireAdmin() {
		if (!"ADMIN".equals(currentUser.role())) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required");
		}
	}
}
