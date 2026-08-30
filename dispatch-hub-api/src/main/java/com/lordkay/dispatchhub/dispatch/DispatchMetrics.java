package com.lordkay.dispatchhub.dispatch;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DispatchMetrics {

	private final Counter success;
	private final Counter retry;
	private final Counter dead;
	private final Counter rateLimited;

	public DispatchMetrics(MeterRegistry registry) {
		this.success = Counter.builder("dispatchhub.delivery.success").description("Successful webhook deliveries")
			.register(registry);
		this.retry = Counter.builder("dispatchhub.delivery.retry").description("Deliveries scheduled for retry")
			.register(registry);
		this.dead = Counter.builder("dispatchhub.delivery.dead").description("Deliveries moved to DEAD")
			.register(registry);
		this.rateLimited = Counter.builder("dispatchhub.delivery.rate_limited")
			.description("Deliveries delayed by rate limiting")
			.register(registry);
	}

	public void success() {
		success.increment();
	}

	public void retry() {
		retry.increment();
	}

	public void dead() {
		dead.increment();
	}

	public void rateLimited() {
		rateLimited.increment();
	}
}
