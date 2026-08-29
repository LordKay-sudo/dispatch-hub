package com.lordkay.dispatchhub.delivery;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID> {

	List<DeliveryAttempt> findByTenantIdAndJobIdOrderByAttemptNumberAsc(UUID tenantId, UUID jobId);
}
