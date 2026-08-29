package com.lordkay.dispatchhub.destination;

import com.lordkay.dispatchhub.security.SsrfGuard;
import com.lordkay.dispatchhub.security.TenantEgressHostRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DestinationUrlValidator {

	private final SsrfGuard ssrfGuard;
	private final TenantEgressHostRepository egressHostRepository;

	public DestinationUrlValidator(SsrfGuard ssrfGuard, TenantEgressHostRepository egressHostRepository) {
		this.ssrfGuard = ssrfGuard;
		this.egressHostRepository = egressHostRepository;
	}

	public String requireAllowedUrl(UUID tenantId, String raw) {
		URI uri = ssrfGuard.requireSafeHttpUri(raw);
		ssrfGuard.assertGloballySafe(uri);
		List<String> patterns = egressHostRepository.findHostPatternsByTenantId(tenantId);
		ssrfGuard.assertTenantAllowlisted(uri, patterns);
		return uri.toString();
	}
}
