package com.lordkay.dispatchhub.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "openai")
public class SpringAiFailureSummarizer implements FailureSummarizer {

	private static final Logger log = LoggerFactory.getLogger(SpringAiFailureSummarizer.class);

	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;
	private final MockFailureSummarizer fallback = new MockFailureSummarizer();

	public SpringAiFailureSummarizer(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper) {
		this.chatClient = chatClientBuilder.build();
		this.objectMapper = objectMapper;
	}

	@Override
	public FailureSummary summarize(FailureContext context) {
		try {
			String prompt = """
					You help operators diagnose webhook delivery failures.
					Using only the sanitized metadata below, reply with a single JSON object and nothing else:
					{"explanation":"...","suggestedAction":"..."}
					Do not invent secrets or payload contents. Keep each string under 280 characters.
					No markdown fences.

					jobStatus=%s
					attemptNumber=%d
					httpStatus=%s
					errorMessage=%s
					""".formatted(context.jobStatus(), context.attemptNumber(),
					context.httpStatus() == null ? "null" : context.httpStatus().toString(),
					sanitize(context.errorMessage()));

			String raw = chatClient.prompt().user(prompt).call().content();
			AiDraft draft = parseDraft(raw);
			if (draft == null || isBlank(draft.explanation()) || isBlank(draft.suggestedAction())) {
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

	private AiDraft parseDraft(String raw) {
		if (raw == null || raw.isBlank()) {
			return null;
		}
		String json = extractJsonObject(raw.trim());
		JsonNode node = objectMapper.readTree(json);
		String explanation = text(node, "explanation");
		String suggestedAction = text(node, "suggestedAction");
		return new AiDraft(explanation, suggestedAction);
	}

	private static String extractJsonObject(String raw) {
		if (raw.startsWith("```")) {
			int start = raw.indexOf('{');
			int end = raw.lastIndexOf('}');
			if (start >= 0 && end > start) {
				return raw.substring(start, end + 1);
			}
		}
		int start = raw.indexOf('{');
		int end = raw.lastIndexOf('}');
		if (start >= 0 && end > start) {
			return raw.substring(start, end + 1);
		}
		return raw;
	}

	private static String text(JsonNode node, String field) {
		JsonNode value = node.get(field);
		return value == null || value.isNull() ? null : value.asString();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
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
