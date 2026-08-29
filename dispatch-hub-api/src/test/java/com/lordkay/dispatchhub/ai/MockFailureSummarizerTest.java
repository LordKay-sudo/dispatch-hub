package com.lordkay.dispatchhub.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MockFailureSummarizerTest {

	@Test
	void buildsOperatorFacingSummary() {
		FailureSummarizer summarizer = new MockFailureSummarizer();
		FailureSummary summary = summarizer.summarize(new FailureContext("DEAD", 3, 500, "upstream timeout"));
		assertTrue(summary.aiGenerated());
		assertEquals("mock", summary.provider());
		assertTrue(summary.explanation().contains("500"));
		assertTrue(summary.suggestedAction().toLowerCase().contains("retry"));
	}
}
