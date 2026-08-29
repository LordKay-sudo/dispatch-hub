package com.lordkay.dispatchhub.destination;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DestinationRepository extends JpaRepository<Destination, UUID> {

	List<Destination> findByTenantIdOrderByNameAsc(UUID tenantId);

	List<Destination> findByTenantIdAndEnabledTrue(UUID tenantId);

	Optional<Destination> findByIdAndTenantId(UUID id, UUID tenantId);

	boolean existsByTenantIdAndNameIgnoreCase(UUID tenantId, String name);
}
