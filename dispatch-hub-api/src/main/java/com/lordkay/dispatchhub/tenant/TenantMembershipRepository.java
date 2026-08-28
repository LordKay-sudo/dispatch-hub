package com.lordkay.dispatchhub.tenant;

import com.lordkay.dispatchhub.user.AppUser;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TenantMembershipRepository extends JpaRepository<TenantMembership, UUID> {

	@Query("""
			select m from TenantMembership m
			join fetch m.tenant
			where m.user.id = :userId
			""")
	List<TenantMembership> findByUserIdWithTenant(@Param("userId") UUID userId);

	@Query("""
			select m from TenantMembership m
			join fetch m.tenant
			join fetch m.user
			where m.user = :user and lower(m.tenant.code) = lower(:tenantCode)
			""")
	Optional<TenantMembership> findByUserAndTenantCode(@Param("user") AppUser user,
			@Param("tenantCode") String tenantCode);

	@Query("""
			select m from TenantMembership m
			join fetch m.tenant
			where m.user.id = :userId and m.tenant.id = :tenantId
			""")
	Optional<TenantMembership> findByUserIdAndTenantId(@Param("userId") UUID userId,
			@Param("tenantId") UUID tenantId);
}
