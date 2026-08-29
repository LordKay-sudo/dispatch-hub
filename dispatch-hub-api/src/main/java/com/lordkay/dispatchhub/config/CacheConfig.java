package com.lordkay.dispatchhub.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

	public static final String DESTINATIONS = "destinations";
	public static final String RATE_LIMIT = "rate-limit";

	@Bean
	CacheManager cacheManager() {
		CaffeineCacheManager manager = new CaffeineCacheManager(DESTINATIONS, RATE_LIMIT);
		manager.setCaffeine(Caffeine.newBuilder().maximumSize(1_000).expireAfterWrite(5, TimeUnit.MINUTES));
		return manager;
	}
}
