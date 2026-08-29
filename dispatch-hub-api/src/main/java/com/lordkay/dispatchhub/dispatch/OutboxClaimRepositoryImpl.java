package com.lordkay.dispatchhub.dispatch;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom Spring Data JDBC fragment for Postgres outbox claiming ({@code FOR UPDATE SKIP LOCKED}).
 */
public class OutboxClaimRepositoryImpl implements OutboxClaimRepositoryCustom {

	private final NamedParameterJdbcTemplate jdbc;

	public OutboxClaimRepositoryImpl(NamedParameterJdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	@Override
	@Transactional
	public List<UUID> claimPending(String workerId, int batchSize) {
		String sql = """
				WITH picked AS (
				    SELECT id
				    FROM delivery_job
				    WHERE status = 'PENDING'
				      AND next_run_at <= NOW()
				    ORDER BY next_run_at
				    FOR UPDATE SKIP LOCKED
				    LIMIT :batchSize
				)
				UPDATE delivery_job j
				SET status = 'RUNNING',
				    locked_at = NOW(),
				    locked_by = :workerId,
				    updated_at = NOW()
				FROM picked
				WHERE j.id = picked.id
				RETURNING j.id
				""";
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("batchSize", batchSize)
				.addValue("workerId", workerId);
		return jdbc.query(sql, params, (rs, rowNum) -> (UUID) rs.getObject("id"));
	}

	@Override
	public void markSuccess(UUID jobId, int attemptCount) {
		jdbc.update("""
				UPDATE delivery_job
				SET status = 'SUCCESS',
				    attempt_count = :attemptCount,
				    last_error = NULL,
				    locked_at = NULL,
				    locked_by = NULL,
				    updated_at = NOW()
				WHERE id = :jobId
				""", Map.of("attemptCount", attemptCount, "jobId", jobId));
	}

	@Override
	public void scheduleRetry(UUID jobId, int attemptCount, String error, Instant nextRunAt) {
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("attemptCount", attemptCount)
				.addValue("error", error)
				.addValue("nextRunAt", Timestamp.from(nextRunAt))
				.addValue("jobId", jobId);
		jdbc.update("""
				UPDATE delivery_job
				SET status = 'PENDING',
				    attempt_count = :attemptCount,
				    last_error = :error,
				    next_run_at = :nextRunAt,
				    locked_at = NULL,
				    locked_by = NULL,
				    updated_at = NOW()
				WHERE id = :jobId
				""", params);
	}

	@Override
	public void markDead(UUID jobId, int attemptCount, String error) {
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("attemptCount", attemptCount)
				.addValue("error", error)
				.addValue("jobId", jobId);
		jdbc.update("""
				UPDATE delivery_job
				SET status = 'DEAD',
				    attempt_count = :attemptCount,
				    last_error = :error,
				    locked_at = NULL,
				    locked_by = NULL,
				    updated_at = NOW()
				WHERE id = :jobId
				""", params);
	}

	@Override
	public void markFailed(UUID jobId, int attemptCount, String error) {
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("attemptCount", attemptCount)
				.addValue("error", error)
				.addValue("jobId", jobId);
		jdbc.update("""
				UPDATE delivery_job
				SET status = 'FAILED',
				    attempt_count = :attemptCount,
				    last_error = :error,
				    locked_at = NULL,
				    locked_by = NULL,
				    updated_at = NOW()
				WHERE id = :jobId
				""", params);
	}

	@Override
	public void requeue(UUID jobId) {
		jdbc.update("""
				UPDATE delivery_job
				SET status = 'PENDING',
				    next_run_at = NOW(),
				    last_error = NULL,
				    locked_at = NULL,
				    locked_by = NULL,
				    updated_at = NOW()
				WHERE id = :jobId
				""", Map.of("jobId", jobId));
	}

	@Override
	public void insertAttempt(UUID attemptId, UUID tenantId, UUID jobId, int attemptNumber, Integer httpStatus,
			long durationMs, String errorMessage) {
		MapSqlParameterSource params = new MapSqlParameterSource()
				.addValue("id", attemptId)
				.addValue("tenantId", tenantId)
				.addValue("jobId", jobId)
				.addValue("attemptNumber", attemptNumber)
				.addValue("httpStatus", httpStatus)
				.addValue("durationMs", durationMs)
				.addValue("errorMessage", errorMessage)
				.addValue("createdAt", Timestamp.from(Instant.now()));
		jdbc.update("""
				INSERT INTO delivery_attempt
				    (id, tenant_id, job_id, attempt_number, http_status, duration_ms, error_message, created_at)
				VALUES (:id, :tenantId, :jobId, :attemptNumber, :httpStatus, :durationMs, :errorMessage, :createdAt)
				""", params);
	}
}
