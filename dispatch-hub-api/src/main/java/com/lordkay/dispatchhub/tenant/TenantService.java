package com.lordkay.dispatchhub.tenant;

import com.lordkay.dispatchhub.common.ApiException;
import com.lordkay.dispatchhub.security.CurrentUser;
import com.lordkay.dispatchhub.tenant.dto.AdminCheckResponse;
import com.lordkay.dispatchhub.tenant.dto.MembershipResponse;
import com.lordkay.dispatchhub.tenant.dto.TenantResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

	private final TenantMembershipRepository membershipRepository;
	private final CurrentUser currentUser;

	public TenantService(TenantMembershipRepository membershipRepository, CurrentUser currentUser) {
		this.membershipRepository = membershipRepository;
		this.currentUser = currentUser;
	}

	@Transactional(readOnly = true)
	public List<TenantResponse> listForCurrentUser() {
		return membershipRepository.findByUserIdWithTenant(currentUser.userId())
			.stream()
			.map(m -> toResponse(m.getTenant()))
			.toList();
	}

	@Transactional(readOnly = true)
	public TenantResponse getForCurrentSession(UUID tenantId) {
		requireSessionTenant(tenantId);
		TenantMembership membership = requireMembership(tenantId);
		return toResponse(membership.getTenant());
	}

	@Transactional(readOnly = true)
	public MembershipResponse membership(UUID tenantId) {
		requireSessionTenant(tenantId);
		TenantMembership membership = requireMembership(tenantId);
		return new MembershipResponse(membership.getTenant().getId(), membership.getTenant().getCode(),
				membership.getRole().name());
	}

	@Transactional(readOnly = true)
	public AdminCheckResponse adminCheck(UUID tenantId) {
		requireSessionTenant(tenantId);
		TenantMembership membership = requireMembership(tenantId);
		if (membership.getRole() != TenantRole.ADMIN) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required");
		}
		return new AdminCheckResponse(true);
	}

	private void requireSessionTenant(UUID tenantId) {
		if (!currentUser.tenantId().equals(tenantId)) {
			throw new ApiException(HttpStatus.FORBIDDEN, "Token is not scoped to this tenant");
		}
	}

	private TenantMembership requireMembership(UUID tenantId) {
		return membershipRepository.findByUserIdAndTenantId(currentUser.userId(), tenantId)
			.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant not found"));
	}

	private static TenantResponse toResponse(Tenant tenant) {
		return new TenantResponse(tenant.getId(), tenant.getCode(), tenant.getName());
	}
}
