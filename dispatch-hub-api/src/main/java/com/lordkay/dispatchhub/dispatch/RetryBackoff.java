package com.lordkay.dispatchhub.dispatch;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RetryBackoff {

	private final int maxAttempts;
	private final long baseDelaySeconds;
	private final long maxDelaySeconds;

	public RetryBackoff(@Value("${app.dispatch.max-attempts:5}") int maxAttempts,
			@Value("${app.dispatch.backoff-base-seconds:2}") long baseDelaySeconds,
			@Value("${app.dispatch.backoff-max-seconds:300}") long maxDelaySeconds) {
		this.maxAttempts = maxAttempts;
		this.baseDelaySeconds = baseDelaySeconds;
		this.maxDelaySeconds = maxDelaySeconds;
	}

	public int maxAttempts() {
		return maxAttempts;
	}

	public boolean shouldRetry(int attemptCount) {
		return attemptCount < maxAttempts;
	}

	public Duration delayAfterAttempt(int attemptCount) {
		long exp = Math.min(attemptCount, 20);
		long seconds = baseDelaySeconds * (1L << Math.max(exp - 1, 0));
		return Duration.ofSeconds(Math.min(seconds, maxDelaySeconds));
	}
}
