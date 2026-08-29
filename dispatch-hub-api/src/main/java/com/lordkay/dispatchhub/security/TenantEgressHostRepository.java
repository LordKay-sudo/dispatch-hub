package com.lordkay.dispatchhub.security;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantEgressHostRepository extends JpaRepository<TenantEgressHost, UUID> {

	@Query("select h.hostPattern from TenantEgressHost h where h.tenantId = :tenantId")
	List<String> findHostPatternsByTenantId(@Param("tenantId") UUID tenantId);
}
