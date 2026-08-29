package com.lordkay.dispatchhub.ai;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/tenants/{tenantId}/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
public class FailureSummaryController {

	private final FailureSummaryService failureSummaryService;

	public FailureSummaryController(FailureSummaryService failureSummaryService) {
		this.failureSummaryService = failureSummaryService;
	}

	@PostMapping("/{jobId}/ai-summary")
	public FailureSummary summarize(@PathVariable UUID tenantId, @PathVariable UUID jobId) {
		return failureSummaryService.summarizeJob(tenantId, jobId);
	}
}
