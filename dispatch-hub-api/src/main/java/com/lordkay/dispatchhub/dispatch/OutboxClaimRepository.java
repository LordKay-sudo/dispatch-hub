package com.lordkay.dispatchhub.dispatch;

import java.util.UUID;
import org.springframework.data.repository.Repository;

/**
 * Spring Data JDBC repository for outbox claim / status updates.
 * Custom SKIP LOCKED SQL lives in {@link OutboxClaimRepositoryCustom}.
 */
public interface OutboxClaimRepository extends Repository<OutboxJob, UUID>, OutboxClaimRepositoryCustom {
}
