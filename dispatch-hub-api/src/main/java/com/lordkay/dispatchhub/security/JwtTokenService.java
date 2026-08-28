package com.lordkay.dispatchhub.security;

import com.lordkay.dispatchhub.tenant.TenantRole;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

	private final JwtEncoder jwtEncoder;
	private final long ttlSeconds;

	public JwtTokenService(JwtEncoder jwtEncoder, @Value("${app.security.jwt.ttl}") java.time.Duration ttl) {
		this.jwtEncoder = jwtEncoder;
		this.ttlSeconds = ttl.toSeconds();
	}

	public IssuedToken issue(UUID userId, String username, UUID tenantId, String tenantCode, TenantRole role) {
		Instant now = Instant.now();
		Instant expires = now.plusSeconds(ttlSeconds);
		JwtClaimsSet claims = JwtClaimsSet.builder()
			.issuedAt(now)
			.expiresAt(expires)
			.subject(username)
			.claim("uid", userId.toString())
			.claim("tenant_id", tenantId.toString())
			.claim("tenant_code", tenantCode)
			.claim("role", role.name())
			.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new IssuedToken(token, ttlSeconds, expires);
	}

	public record IssuedToken(String accessToken, long expiresInSeconds, Instant expiresAt) {
	}
}
