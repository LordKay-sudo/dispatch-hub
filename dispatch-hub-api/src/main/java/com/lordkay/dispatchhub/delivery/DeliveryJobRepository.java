package com.lordkay.dispatchhub.delivery;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryJobRepository extends JpaRepository<DeliveryJob, UUID> {

	List<DeliveryJob> findByTenantIdAndEventIdOrderByCreatedAtAsc(UUID tenantId, UUID eventId);

	Optional<DeliveryJob> findByIdAndTenantId(UUID id, UUID tenantId);

	long countByTenantIdAndStatus(UUID tenantId, DeliveryJobStatus status);

	long countByTenantId(UUID tenantId);
}
