package com.lordkay.dispatchhub.dispatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class RetryBackoffTest {

	private final RetryBackoff backoff = new RetryBackoff(5, 2, 300);

	@Test
	void retriesUntilMaxAttempts() {
		assertTrue(backoff.shouldRetry(1));
		assertTrue(backoff.shouldRetry(4));
		assertFalse(backoff.shouldRetry(5));
	}

	@Test
	void delayGrowsThenCaps() {
		assertEquals(Duration.ofSeconds(2), backoff.delayAfterAttempt(1));
		assertEquals(Duration.ofSeconds(4), backoff.delayAfterAttempt(2));
		assertEquals(Duration.ofSeconds(8), backoff.delayAfterAttempt(3));
		assertEquals(Duration.ofSeconds(300), backoff.delayAfterAttempt(20));
	}
}
