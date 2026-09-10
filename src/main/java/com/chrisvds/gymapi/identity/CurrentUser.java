package com.chrisvds.gymapi.identity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the "owner" every row is scoped to.
 *
 * <p>In production the app sits behind Cloudflare Access, which injects
 * {@code Cf-Access-Authenticated-User-Email} on every request that made it past
 * the edge login. We use that as the owner key, so a second Access user gets
 * their own isolated data with no password code in this service.
 *
 * <p>Locally (no Access in front) there's no header, so we fall back to
 * {@code gym-api.default-owner} (default {@code "local"}).
 */
@Component
public class CurrentUser {

	static final String ACCESS_EMAIL_HEADER = "Cf-Access-Authenticated-User-Email";

	private final String defaultOwner;

	public CurrentUser(@Value("${gym-api.default-owner:local}") String defaultOwner) {
		this.defaultOwner = defaultOwner;
	}

	/** The owner key for the request in flight, or the default outside a request. */
	public String owner() {
		RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
		if (attrs instanceof org.springframework.web.context.request.ServletRequestAttributes sra) {
			HttpServletRequest req = sra.getRequest();
			String email = req.getHeader(ACCESS_EMAIL_HEADER);
			if (email != null && !email.isBlank()) {
				return email.trim().toLowerCase();
			}
		}
		return defaultOwner;
	}
}
