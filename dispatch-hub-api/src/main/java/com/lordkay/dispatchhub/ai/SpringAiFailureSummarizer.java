package com.lordkay.dispatchhub.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class SpringAiFailureSummarizer implements FailureSummarizer {

	private static final Logger log = LoggerFactory.getLogger(SpringAiFailureSummarizer.class);

	private final ChatClient chatClient;
	private final MockFailureSummarizer fallback = new MockFailureSummarizer();

	public SpringAiFailureSummarizer(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	@Override
	public FailureSummary summarize(FailureContext context) {
		try {
			String prompt = """
					You help operators diagnose webhook delivery failures.
					Using only the sanitized metadata below, return two short paragraphs:
					1) explanation
					2) suggestedAction
					Do not invent secrets or payload contents. Keep each under 280 characters.

					jobStatus=%s
					attemptNumber=%d
					httpStatus=%s
					errorMessage=%s
					""".formatted(context.jobStatus(), context.attemptNumber(),
					context.httpStatus() == null ? "null" : context.httpStatus().toString(),
					sanitize(context.errorMessage()));

			AiDraft draft = chatClient.prompt().user(prompt).call().entity(AiDraft.class);
			if (draft == null || draft.explanation() == null || draft.suggestedAction() == null) {
				return fallback.summarize(context);
			}
			return new FailureSummary(draft.explanation().trim(), draft.suggestedAction().trim(), true, "openai");
		}
		catch (Exception ex) {
			log.warn("OpenAI failure summary unavailable, using fallback", ex);
			FailureSummary mock = fallback.summarize(context);
			return new FailureSummary(mock.explanation() + " (AI provider unavailable; fallback used)",
					mock.suggestedAction(), true, "openai-fallback");
		}
	}

	private static String sanitize(String errorMessage) {
		if (errorMessage == null || errorMessage.isBlank()) {
			return "none";
		}
		String cleaned = errorMessage.replaceAll("(?i)(authorization|api[_-]?key|secret|token)\\s*[:=]\\s*\\S+",
				"$1=[redacted]");
		return cleaned.length() <= 400 ? cleaned : cleaned.substring(0, 400);
	}

	public record AiDraft(String explanation, String suggestedAction) {
	}
}
