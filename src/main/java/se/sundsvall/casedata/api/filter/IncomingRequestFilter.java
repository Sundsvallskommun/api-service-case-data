package se.sundsvall.casedata.api.filter;

import static java.util.Objects.isNull;
import static se.sundsvall.casedata.service.util.Constants.UNKNOWN;
import static se.sundsvall.casedata.service.util.Constants.X_JWT_ASSERTION_HEADER_KEY;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class IncomingRequestFilter extends OncePerRequestFilter {

	private static final ThreadLocal<String> THREAD_LOCAL_INSTANCE = new ThreadLocal<>();

	@Override
	protected void doFilterInternal(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response, final FilterChain filterChain) throws IOException, ServletException {
		// Extract sub from x-jwt-assertion header
		try {
			extractSubscriber(request);
			filterChain.doFilter(request, response);
		} finally {
			if (THREAD_LOCAL_INSTANCE.get() != null) {
				THREAD_LOCAL_INSTANCE.remove();
			}
		}
	}

	private void extractSubscriber(final HttpServletRequest request) throws JsonProcessingException {
		final var jwtHeader = request.getHeader(X_JWT_ASSERTION_HEADER_KEY);

		if (isNull(jwtHeader) || jwtHeader.isBlank()) {
			THREAD_LOCAL_INSTANCE.set(UNKNOWN);
		} else {
			final String[] jwtParts = jwtHeader.split("\\.");
			final String jwtPayload = new String(Base64.getUrlDecoder().decode(jwtParts[1]));
			THREAD_LOCAL_INSTANCE.set(new ObjectMapper().readTree(jwtPayload).findValue("sub").asText());
		}
	}

	public String getSubscriber() {
		return THREAD_LOCAL_INSTANCE.get();
	}
}
