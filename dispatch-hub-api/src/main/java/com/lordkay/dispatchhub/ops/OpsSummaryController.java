package com.lordkay.dispatchhub.ops;

import com.lordkay.dispatchhub.ops.dto.OpsSummaryResponse;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/tenants/{tenantId}/ops-summary", produces = MediaType.APPLICATION_JSON_VALUE)
public class OpsSummaryController {

	private final OpsSummaryService opsSummaryService;

	public OpsSummaryController(OpsSummaryService opsSummaryService) {
		this.opsSummaryService = opsSummaryService;
	}

	@GetMapping
	public OpsSummaryResponse summary(@PathVariable UUID tenantId) {
		return opsSummaryService.summarize(tenantId);
	}
}
