package com.lordkay.dispatchhub.dispatch;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxClaimRepository {

	private final JdbcTemplate jdbcTemplate;

	public OutboxClaimRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<UUID> claimPending(String workerId, int batchSize) {
		String sql = """
				WITH picked AS (
				    SELECT id
				    FROM delivery_job
				    WHERE status = 'PENDING'
				      AND next_run_at <= NOW()
				    ORDER BY next_run_at
				    FOR UPDATE SKIP LOCKED
				    LIMIT ?
				)
				UPDATE delivery_job j
				SET status = 'RUNNING',
				    locked_at = NOW(),
				    locked_by = ?,
				    updated_at = NOW()
				FROM picked
				WHERE j.id = picked.id
				RETURNING j.id
				""";
		return jdbcTemplate.query(sql, (rs, rowNum) -> (UUID) rs.getObject("id"), batchSize, workerId);
	}

	public void markSuccess(UUID jobId, int attemptCount) {
		jdbcTemplate.update("""
				UPDATE delivery_job
				SET status = 'SUCCESS',
				    attempt_count = ?,
				    last_error = NULL,
				    locked_at = NULL,
				    locked_by = NULL,
				    updated_at = NOW()
				WHERE id = ?
				""", attemptCount, jobId);
	}

	public void markFailed(UUID jobId, int attemptCount, String error) {
		jdbcTemplate.update("""
				UPDATE delivery_job
				SET status = 'FAILED',
				    attempt_count = ?,
				    last_error = ?,
				    locked_at = NULL,
				    locked_by = NULL,
				    updated_at = NOW()
				WHERE id = ?
				""", attemptCount, error, jobId);
	}

	public void insertAttempt(UUID attemptId, UUID tenantId, UUID jobId, int attemptNumber, Integer httpStatus,
			long durationMs, String errorMessage) {
		jdbcTemplate.update("""
				INSERT INTO delivery_attempt
				    (id, tenant_id, job_id, attempt_number, http_status, duration_ms, error_message, created_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, ?)
				""", attemptId, tenantId, jobId, attemptNumber, httpStatus, durationMs, errorMessage,
				Timestamp.from(Instant.now()));
	}
}
