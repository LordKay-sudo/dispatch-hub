package com.lordkay.dispatchhub.destination;

import com.lordkay.dispatchhub.common.ApiException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class DestinationUrlValidator {

	public String requireHttpUrl(String raw) {
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
		if (uri.getHost() == null || uri.getHost().isBlank()) {
			throw new ApiException(HttpStatus.BAD_REQUEST, "targetUrl must include a host");
		}
		return uri.toString();
	}
}
