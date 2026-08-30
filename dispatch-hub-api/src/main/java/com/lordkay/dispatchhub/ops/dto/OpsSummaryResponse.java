package com.lordkay.dispatchhub.ops.dto;

public record OpsSummaryResponse(
		long totalJobs,
		long pending,
		long running,
		long success,
		long failed,
		long dead) {
}
