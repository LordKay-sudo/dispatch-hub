package com.lordkay.dispatchhub.dispatch;

public record WebhookSendResult(boolean success, Integer httpStatus, long durationMs, String errorMessage) {
}
