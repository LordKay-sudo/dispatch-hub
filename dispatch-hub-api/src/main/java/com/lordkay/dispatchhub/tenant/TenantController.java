package com.lordkay.dispatchhub.tenant;

import com.lordkay.dispatchhub.tenant.dto.AdminCheckResponse;
import com.lordkay.dispatchhub.tenant.dto.MembershipResponse;
import com.lordkay.dispatchhub.tenant.dto.TenantResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/tenants", produces = MediaType.APPLICATION_JSON_VALUE)
public class TenantController {

	private final TenantService tenantService;

	public TenantController(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	@GetMapping
	public List<TenantResponse> list() {
		return tenantService.listForCurrentUser();
	}

	@GetMapping("/{tenantId}")
	public TenantResponse get(@PathVariable UUID tenantId) {
		return tenantService.getForCurrentSession(tenantId);
	}

	@GetMapping("/{tenantId}/membership")
	public MembershipResponse membership(@PathVariable UUID tenantId) {
		return tenantService.membership(tenantId);
	}

	@PostMapping("/{tenantId}/admin-check")
	@PreAuthorize("hasRole('ADMIN')")
	public AdminCheckResponse adminCheck(@PathVariable UUID tenantId) {
		return tenantService.adminCheck(tenantId);
	}
}
