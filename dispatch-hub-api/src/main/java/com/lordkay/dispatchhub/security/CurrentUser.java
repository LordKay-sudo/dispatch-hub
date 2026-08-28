package com.lordkay.dispatchhub.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

	public Jwt requireJwt() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof JwtAuthenticationToken jwtAuth) {
			return jwtAuth.getToken();
		}
		throw new IllegalStateException("Expected JWT authentication");
	}

	public UUID userId() {
		return UUID.fromString(requireJwt().getClaimAsString("uid"));
	}

	public UUID tenantId() {
		return UUID.fromString(requireJwt().getClaimAsString("tenant_id"));
	}

	public String tenantCode() {
		return requireJwt().getClaimAsString("tenant_code");
	}

	public String role() {
		return requireJwt().getClaimAsString("role");
	}

	public String username() {
		return requireJwt().getSubject();
	}
}
