package com.lordkay.dispatchhub.dispatch;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DestinationRateLimiter {

	private final Cache<String, AtomicInteger> counters;
	private final int maxPerWindow;

	public DestinationRateLimiter(@Value("${app.dispatch.rate-limit-per-minute:60}") int maxPerWindow) {
		this.maxPerWindow = maxPerWindow;
		this.counters = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(10_000).build();
	}

	public boolean tryAcquire(UUID tenantId, UUID destinationId) {
		String key = tenantId + ":" + destinationId;
		AtomicInteger counter = counters.get(key, ignored -> new AtomicInteger(0));
		return counter.incrementAndGet() <= maxPerWindow;
	}
}
