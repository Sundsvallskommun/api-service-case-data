package se.sundsvall.casedata.service.util;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.dept44.problem.Problem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Single source of truth for reading the raw bytes of an attachment during the base64-to-binary migration: prefer the
 * binary {@code content} blob and fall back to decoding the legacy base64 {@code file} column for rows not yet
 * migrated.
 *
 * <p>
 * Callers that can stream (e.g. the download endpoint) should read the blob directly rather than materialising it via
 * {@link #toBytes(AttachmentEntity)}; this helper exists for the legacy decode and for callers that genuinely need a
 * {@code byte[]} (e.g. building a multipart file).
 */
public final class AttachmentContents {

	private AttachmentContents() {}

	/**
	 * Returns the raw bytes of the attachment, preferring the binary {@code content} blob and falling back to decoding the
	 * legacy base64 {@code file} column. Materialises the content in memory.
	 */
	public static byte[] toBytes(final AttachmentEntity attachment) {
		final var blob = attachment.getContent();
		if (blob != null) {
			return readBlob(blob, attachment.getId());
		}
		return decodeBase64(attachment.getFile(), attachment.getId());
	}

	/**
	 * Decodes the legacy base64 {@code file} content. A null or blank value yields an empty array; malformed base64 is
	 * reported as an internal error referencing the attachment id.
	 */
	public static byte[] decodeBase64(final String base64Content, final Object attachmentId) {
		if (base64Content == null || base64Content.isBlank()) {
			return new byte[0];
		}
		try {
			return Base64.getDecoder().decode(base64Content.getBytes(UTF_8));
		} catch (final IllegalArgumentException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Attachment with id '%s' has malformed base64 content and cannot be read: %s".formatted(attachmentId, e.getMessage()));
		}
	}

	private static byte[] readBlob(final Blob blob, final Object attachmentId) {
		try (final var in = blob.getBinaryStream()) {
			return in.readAllBytes();
		} catch (final IOException | SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s occurred when reading content of attachment with id '%s': %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
		}
	}
}
