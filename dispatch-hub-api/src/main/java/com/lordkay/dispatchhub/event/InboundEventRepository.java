package com.lordkay.dispatchhub.event;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundEventRepository extends JpaRepository<InboundEvent, UUID> {

	Optional<InboundEvent> findByTenantIdAndIdempotencyKey(UUID tenantId, String idempotencyKey);

	Optional<InboundEvent> findByIdAndTenantId(UUID id, UUID tenantId);

	List<InboundEvent> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
