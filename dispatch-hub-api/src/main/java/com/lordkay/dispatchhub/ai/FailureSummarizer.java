package com.lordkay.dispatchhub.ai;

public interface FailureSummarizer {

	FailureSummary summarize(FailureContext context);
}
