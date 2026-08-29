package com.lordkay.dispatchhub.ai;

public record FailureSummary(String explanation, String suggestedAction, boolean aiGenerated, String provider) {
}
