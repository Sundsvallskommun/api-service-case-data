package se.sundsvall.casedata.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.dept44.problem.Problem;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Handling of the JSON metadata part of a multipart attachment upload, shared by the errand and decision attachment
 * resources. Bean validation of a {@code @RequestPart} String is not performed by the framework, so the parsed metadata
 * has to be validated explicitly.
 */
final class AttachmentMetadataParts {

	private AttachmentMetadataParts() {}

	/**
	 * Parses the 'attachment' metadata part. Jackson's parse failures are unchecked, so without this they would escape as
	 * an unhandled exception and be reported as a server error - unparsable input from a client is a bad request. The
	 * parser message is deliberately not echoed back, to avoid reflecting the submitted payload in the response.
	 */
	static Attachment parse(final ObjectMapper objectMapper, final String attachment) {
		try {
			return objectMapper.readValue(attachment, Attachment.class);
		} catch (final JacksonException e) {
			throw Problem.valueOf(BAD_REQUEST, "The 'attachment' part must be valid JSON");
		}
	}

	/**
	 * Validates the parsed metadata, reporting violations in a stable (property path) order so the response does not vary
	 * between otherwise identical requests.
	 */
	static <T> void validate(final Validator validator, final T object) {
		final Set<ConstraintViolation<T>> violations = validator.validate(object);
		if (!violations.isEmpty()) {
			final var sorted = violations.stream()
				.sorted(Comparator.comparing(v -> v.getPropertyPath().toString()))
				.collect(Collectors.toCollection(LinkedHashSet::new));
			throw new ConstraintViolationException(sorted);
		}
	}
}
