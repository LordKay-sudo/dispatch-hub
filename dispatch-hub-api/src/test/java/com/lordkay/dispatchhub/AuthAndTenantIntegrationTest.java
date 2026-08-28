package com.lordkay.dispatchhub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthAndTenantIntegrationTest {

	private static final String ACME_ID = "11111111-1111-1111-1111-111111111111";
	private static final String BETA_ID = "22222222-2222-2222-2222-222222222222";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void loginSucceedsForDemoAdmin() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"admin.acme","password":"password","tenantCode":"acme"}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.role").value("ADMIN"))
			.andExpect(jsonPath("$.tenantCode").value("acme"));
	}

	@Test
	void loginFailsForBadPassword() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
			.content("""
					{"username":"admin.acme","password":"wrong","tenantCode":"acme"}
					"""))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void tenantApisRespectJwtScopeAndRoles() throws Exception {
		String adminToken = login("admin.acme", "password", "acme");
		String viewerToken = login("viewer.acme", "password", "acme");

		mockMvc.perform(get("/api/v1/tenants").header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].code").value("acme"));

		mockMvc.perform(get("/api/v1/tenants/" + ACME_ID).header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.code").value("acme"));

		mockMvc.perform(get("/api/v1/tenants/" + BETA_ID).header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/v1/tenants/" + ACME_ID + "/admin-check")
			.header("Authorization", "Bearer " + adminToken))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.ok").value(true));

		mockMvc.perform(post("/api/v1/tenants/" + ACME_ID + "/admin-check")
			.header("Authorization", "Bearer " + viewerToken))
			.andExpect(status().isForbidden());
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
