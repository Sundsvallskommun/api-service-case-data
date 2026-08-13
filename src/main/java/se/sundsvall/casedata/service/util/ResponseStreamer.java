package se.sundsvall.casedata.service.util;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import org.springframework.util.StreamUtils;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Streams binary attachment content to an {@link HttpServletResponse} as a file download. Centralises the
 * (content-type, content-disposition, content-length, copy) handling shared by the attachment download endpoints.
 */
public final class ResponseStreamer {

	private ResponseStreamer() {}

	/**
	 * Streams an attachment's binary content. An attachment stored without content is served as an empty body. The id is
	 * supplied by the caller rather than read from the entity, so a failure is reported against the id the request asked
	 * for.
	 */
	public static void streamAttachment(final HttpServletResponse response, final AttachmentEntity attachmentEntity, final Long attachmentId) {
		final var blob = attachmentEntity.getContent();
		if (blob != null) {
			streamBlob(response, attachmentEntity.getName(), attachmentEntity.getMimeType(), blob, attachmentId);
		} else {
			streamBytes(response, attachmentEntity.getName(), attachmentEntity.getMimeType(), new byte[0], attachmentId);
		}
	}

	/**
	 * Streams the content of a {@link Blob}. The headers are written before the blob is touched, so a failure while
	 * reading the blob still leaves the (already added) headers on the response - mirroring the previous inline behaviour.
	 */
	public static void streamBlob(final HttpServletResponse response, final String fileName, final String mimeType, final Blob blob, final Object attachmentId) {
		try {
			addHeaders(response, fileName, mimeType);
			response.setContentLengthLong(blob.length());
			try (final InputStream in = blob.getBinaryStream()) {
				StreamUtils.copy(in, response.getOutputStream());
			}
		} catch (final IOException | SQLException e) {
			throw copyFailed(attachmentId, e);
		}
	}

	/**
	 * Streams already-materialised bytes.
	 */
	public static void streamBytes(final HttpServletResponse response, final String fileName, final String mimeType, final byte[] content, final Object attachmentId) {
		try (final InputStream in = new ByteArrayInputStream(content)) {
			addHeaders(response, fileName, mimeType);
			response.setContentLengthLong(content.length);
			StreamUtils.copy(in, response.getOutputStream());
		} catch (final IOException e) {
			throw copyFailed(attachmentId, e);
		}
	}

	private static void addHeaders(final HttpServletResponse response, final String fileName, final String mimeType) {
		response.addHeader(CONTENT_TYPE, mimeType);
		response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
	}

	private static ThrowableProblem copyFailed(final Object attachmentId, final Exception e) {
		return Problem.valueOf(INTERNAL_SERVER_ERROR, "%s occurred when copying file with attachment id '%s' to response: %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
	}
}
