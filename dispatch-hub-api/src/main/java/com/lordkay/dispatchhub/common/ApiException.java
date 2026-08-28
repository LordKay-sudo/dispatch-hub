package com.lordkay.dispatchhub.common;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;

public class ApiException extends ErrorResponseException {

	public ApiException(HttpStatus status, String detail) {
		super(status);
		setTitle(status.getReasonPhrase());
		getBody().setDetail(detail);
	}
}
