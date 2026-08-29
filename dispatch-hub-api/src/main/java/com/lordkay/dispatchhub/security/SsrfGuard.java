package com.lordkay.dispatchhub.security;

import com.lordkay.dispatchhub.common.ApiException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class SsrfGuard {

	private final boolean allowLoopback;

	public SsrfGuard(@Value("${app.security.ssrf.allow-loopback:true}") boolean allowLoopback) {
		this.allowLoopback = allowLoopback;
	}

	public URI requireSafeHttpUri(String raw) {
		URI uri;
		try {
			uri = URI.create(raw.trim());
		}
		catch (IllegalArgumentException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "targetUrl is not a valid URI");
		}
		String scheme = uri.getScheme();
		if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "targetUrl must use http or https");
		}
		String host = uri.getHost();
		if (host == null || host.isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "targetUrl must include a host");
		}
		return uri;
	}

	public void assertGloballySafe(URI uri) {
		String host = uri.getHost();
		InetAddress[] addresses;
		try {
			addresses = InetAddress.getAllByName(host);
		}
		catch (UnknownHostException ex) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "targetUrl host could not be resolved");
		}
		for (InetAddress address : addresses) {
			if (isBlocked(address)) {
				throw new ApiException(HttpStatus.BAD_REQUEST,
						"targetUrl resolves to a blocked address (SSRF protection)");
			}
		}
	}

	public void assertTenantAllowlisted(URI uri, List<String> hostPatterns) {
		String host = uri.getHost().toLowerCase(Locale.ROOT);
		boolean matched = hostPatterns.stream().anyMatch(pattern -> matchesHost(host, pattern));
		if (!matched) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "targetUrl host is not on the tenant egress allowlist");
		}
	}

	private boolean isBlocked(InetAddress address) {
		if (address.isAnyLocalAddress() || address.isMulticastAddress()) {
			return true;
		}
		if (address.isLinkLocalAddress()) {
			return true;
		}
		byte[] bytes = address.getAddress();
		if (isCloudMetadata(bytes)) {
			return true;
		}
		if (address.isLoopbackAddress()) {
			return !allowLoopback;
		}
		if (address.isSiteLocalAddress()) {
			return true;
		}
		return false;
	}

	private static boolean isCloudMetadata(byte[] bytes) {
		// 169.254.169.254 and broader link-local already caught; keep explicit metadata IP check
		return bytes.length == 4 && (bytes[0] & 0xff) == 169 && (bytes[1] & 0xff) == 254 && (bytes[2] & 0xff) == 169
				&& (bytes[3] & 0xff) == 254;
	}

	private static boolean matchesHost(String host, String pattern) {
		String normalized = pattern.trim().toLowerCase(Locale.ROOT);
		if (normalized.startsWith("*.")) {
			String suffix = normalized.substring(1);
			return host.endsWith(suffix) && host.length() > suffix.length();
		}
		return host.equals(normalized);
	}
}
