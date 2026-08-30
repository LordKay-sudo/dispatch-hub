package com.lordkay.dispatchhub.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockFailureSummarizer implements FailureSummarizer {

	@Override
	public FailureSummary summarize(FailureContext context) {
		String status = context.httpStatus() == null ? "no HTTP status" : "HTTP " + context.httpStatus();
		String error = context.errorMessage() == null || context.errorMessage().isBlank() ? "unspecified error"
				: context.errorMessage();
		String explanation = "Delivery attempt %d ended with %s (%s). Job status is %s."
			.formatted(context.attemptNumber(), status, truncate(error), context.jobStatus());
		String action = "Confirm the destination URL is reachable, review recent attempt logs, then use Retry if the destination is healthy.";
		return new FailureSummary(explanation, action, true, "mock");
	}

	private static String truncate(String value) {
		return value.length() <= 180 ? value : value.substring(0, 180) + "...";
	}
}
