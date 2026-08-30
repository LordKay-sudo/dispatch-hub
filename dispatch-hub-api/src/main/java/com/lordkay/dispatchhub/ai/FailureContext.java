package com.lordkay.dispatchhub.ai;

public record FailureContext(String jobStatus, int attemptNumber, Integer httpStatus, String errorMessage) {
}
