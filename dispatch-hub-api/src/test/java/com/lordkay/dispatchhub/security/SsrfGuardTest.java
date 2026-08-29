package com.lordkay.dispatchhub.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.lordkay.dispatchhub.common.ApiException;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;

class SsrfGuardTest {

	@Test
	void blocksMetadataHostWhenResolvedWouldBeUnsafe() {
		SsrfGuard guard = new SsrfGuard(false);
		URI uri = guard.requireSafeHttpUri("http://example.com/hook");
		assertDoesNotThrow(() -> guard.assertTenantAllowlisted(uri, List.of("example.com")));
	}

	@Test
	void rejectsHostNotOnAllowlist() {
		SsrfGuard guard = new SsrfGuard(true);
		URI uri = guard.requireSafeHttpUri("http://evil.example/hook");
		assertThrows(ApiException.class, () -> guard.assertTenantAllowlisted(uri, List.of("localhost")));
	}

	@Test
	void rejectsNonHttpScheme() {
		SsrfGuard guard = new SsrfGuard(true);
		assertThrows(ApiException.class, () -> guard.requireSafeHttpUri("file:///etc/passwd"));
	}
}
