package com.lordkay.dispatchhub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class DestinationAndOutboxIntegrationTest {

	private static final String ACME_ID = "11111111-1111-1111-1111-111111111111";

	private static HttpServer webhookServer;
	private static int webhookPort;
	private static final AtomicInteger hits = new AtomicInteger();

	@Autowired
	private MockMvc mockMvc;

	@BeforeAll
	static void startWebhook() throws IOException {
		webhookServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		webhookServer.createContext("/hook", exchange -> {
			hits.incrementAndGet();
			byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, body.length);
			try (OutputStream os = exchange.getResponseBody()) {
				os.write(body);
			}
		});
		webhookServer.start();
		webhookPort = webhookServer.getAddress().getPort();
	}

	@AfterAll
	static void stopWebhook() {
		if (webhookServer != null) {
			webhookServer.stop(0);
		}
	}

	@Test
	void createDestinationSubmitEventAndDispatch() throws Exception {
		hits.set(0);
		String token = login("admin.acme", "password", "acme");
		String targetUrl = "http://127.0.0.1:" + webhookPort + "/hook";

		MvcResult destinationResult = mockMvc
			.perform(post("/api/v1/tenants/" + ACME_ID + "/destinations")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"local-hook","targetUrl":"%s","enabled":true}
						""".formatted(targetUrl)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("local-hook"))
			.andReturn();
		String destinationId = JsonPath.read(destinationResult.getResponse().getContentAsString(), "$.id");

		String idempotencyKey = "evt-" + UUID.randomUUID();
		MvcResult eventResult = mockMvc
			.perform(post("/api/v1/tenants/" + ACME_ID + "/events")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"idempotencyKey":"%s","payload":{"hello":"world"},"destinationId":"%s"}
						""".formatted(idempotencyKey, destinationId)))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.jobs[0].status").value("PENDING"))
			.andReturn();
		String eventId = JsonPath.read(eventResult.getResponse().getContentAsString(), "$.id");
		String jobId = JsonPath.read(eventResult.getResponse().getContentAsString(), "$.jobs[0].id");

		mockMvc
			.perform(post("/api/v1/tenants/" + ACME_ID + "/events")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"idempotencyKey":"%s","payload":{"hello":"again"},"destinationId":"%s"}
						""".formatted(idempotencyKey, destinationId)))
			.andExpect(status().isAccepted())
			.andExpect(jsonPath("$.id").value(eventId));

		awaitSuccess(token, eventId);

		mockMvc
			.perform(get("/api/v1/tenants/" + ACME_ID + "/jobs/" + jobId + "/attempts")
				.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].httpStatus").value(200));

		org.junit.jupiter.api.Assertions.assertTrue(hits.get() >= 1);
	}

	@Test
	void viewerCannotCreateDestination() throws Exception {
		String token = login("viewer.acme", "password", "acme");
		mockMvc
			.perform(post("/api/v1/tenants/" + ACME_ID + "/destinations")
				.header("Authorization", "Bearer " + token)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name":"blocked","targetUrl":"http://127.0.0.1:9/hook","enabled":true}
						"""))
			.andExpect(status().isForbidden());
	}

	private void awaitSuccess(String token, String eventId) throws Exception {
		long deadline = System.currentTimeMillis() + 15_000;
		while (System.currentTimeMillis() < deadline) {
			MvcResult result = mockMvc
				.perform(get("/api/v1/tenants/" + ACME_ID + "/events/" + eventId)
					.header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andReturn();
			String status = JsonPath.read(result.getResponse().getContentAsString(), "$.jobs[0].status");
			if ("SUCCESS".equals(status)) {
				return;
			}
			Thread.sleep(500);
		}
		org.junit.jupiter.api.Assertions.fail("Delivery job did not reach SUCCESS in time");
	}

	private String login(String username, String password, String tenantCode) throws Exception {
		MvcResult result = mockMvc
			.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"username":"%s","password":"%s","tenantCode":"%s"}
						""".formatted(username, password, tenantCode)))
			.andExpect(status().isOk())
			.andReturn();
		return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
	}
}
