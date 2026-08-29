package com.lordkay.dispatchhub.dispatch;

import java.time.Duration;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class WebhookClient {

	private final RestClient restClient;

	public WebhookClient() {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		factory.setReadTimeout(Duration.ofSeconds(5));
		this.restClient = RestClient.builder().requestFactory(factory).build();
	}

	public WebhookSendResult send(String targetUrl, String payloadJson, String secret) {
		long started = System.nanoTime();
		try {
			var spec = restClient.post().uri(targetUrl).contentType(MediaType.APPLICATION_JSON).body(payloadJson);
			if (secret != null && !secret.isBlank()) {
				spec = spec.header("X-Webhook-Secret", secret);
			}
			var response = spec.retrieve().toBodilessEntity();
			long durationMs = (System.nanoTime() - started) / 1_000_000L;
			int status = response.getStatusCode().value();
			boolean success = response.getStatusCode().is2xxSuccessful();
			return new WebhookSendResult(success, status, durationMs, success ? null : "HTTP " + status);
		}
		catch (RestClientResponseException ex) {
			long durationMs = (System.nanoTime() - started) / 1_000_000L;
			return new WebhookSendResult(false, ex.getStatusCode().value(), durationMs, truncate(ex.getMessage()));
		}
		catch (Exception ex) {
			long durationMs = (System.nanoTime() - started) / 1_000_000L;
			return new WebhookSendResult(false, null, durationMs, truncate(ex.getMessage()));
		}
	}

	private static String truncate(String message) {
		if (message == null) {
			return "dispatch failed";
		}
		return message.length() <= 2000 ? message : message.substring(0, 2000);
	}
}
