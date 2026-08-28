package com.lordkay.dispatchhub.auth;

import com.lordkay.dispatchhub.auth.dto.LoginRequest;
import com.lordkay.dispatchhub.auth.dto.LoginResponse;
import com.lordkay.dispatchhub.common.ApiException;
import com.lordkay.dispatchhub.security.JwtTokenService;
import com.lordkay.dispatchhub.tenant.TenantMembership;
import com.lordkay.dispatchhub.tenant.TenantMembershipRepository;
import com.lordkay.dispatchhub.user.AppUser;
import com.lordkay.dispatchhub.user.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final AppUserRepository appUserRepository;
	private final TenantMembershipRepository membershipRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;

	public AuthService(AppUserRepository appUserRepository, TenantMembershipRepository membershipRepository,
			PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
		this.appUserRepository = appUserRepository;
		this.membershipRepository = membershipRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		AppUser user = appUserRepository.findByUsernameIgnoreCase(request.username().trim())
			.filter(AppUser::isEnabled)
			.orElseThrow(() -> unauthorized());

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw unauthorized();
		}

		TenantMembership membership = membershipRepository
			.findByUserAndTenantCode(user, request.tenantCode().trim())
			.orElseThrow(() -> unauthorized());

		JwtTokenService.IssuedToken token = jwtTokenService.issue(user.getId(), user.getUsername(),
				membership.getTenant().getId(), membership.getTenant().getCode(), membership.getRole());

		return new LoginResponse(token.accessToken(), "Bearer", token.expiresInSeconds(), user.getUsername(),
				membership.getTenant().getId(), membership.getTenant().getCode(), membership.getRole().name());
	}

	private static ApiException unauthorized() {
		return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials or tenant membership");
	}
}
