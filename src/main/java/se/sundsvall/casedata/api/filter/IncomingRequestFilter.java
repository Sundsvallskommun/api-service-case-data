package se.sundsvall.casedata.api.filter;

import static java.util.Objects.isNull;
import static se.sundsvall.casedata.service.util.Constants.AD_USER_HEADER_KEY;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import java.io.IOException;
import java.util.Base64;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

@Component
public class IncomingRequestFilter extends OncePerRequestFilter {

	// WSO2-subscriber
	@Getter
	private String subscriber;

	// AD-user
	@Getter
	private String adUser;

	@Override
	protected void doFilterInternal(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		// Extract sub from x-jwt-assertion header
		extractSubscriber(request);
		// Extract AD-user from ad-user header
		extractAdUser(request);

		filterChain.doFilter(request, response);
	}

	private void extractAdUser(final HttpServletRequest request) {
		final var headerValue = request.getHeader(AD_USER_HEADER_KEY);

		if (isNull(headerValue) || headerValue.isBlank()) {
			adUser = UNKNOWN;
		} else {
			adUser = headerValue;
		}
	}

	private void extractSubscriber(final HttpServletRequest request) throws JsonProcessingException {
		final var jwtHeader = request.getHeader(X_JWT_ASSERTION_HEADER_KEY);

		if (isNull(jwtHeader) || jwtHeader.isBlank()) {
			subscriber = UNKNOWN;
		} else {
			final String[] jwtParts = jwtHeader.split("\\.");
			final String jwtPayload = new String(Base64.getUrlDecoder().decode(jwtParts[1]));
			subscriber = new ObjectMapper().readTree(jwtPayload).findValue("sub").asText();
		}
	}

}
