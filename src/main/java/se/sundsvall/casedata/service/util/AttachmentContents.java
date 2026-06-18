package se.sundsvall.casedata.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HexFormat;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.dept44.problem.Problem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Single source of truth for an attachment's raw bytes and their content hash during the base64-to-binary migration.
 *
 * <p>
 * For reading bytes it prefers the binary {@code content} blob and falls back to decoding the legacy base64
 * {@code file}
 * column for rows not yet migrated. Callers that can stream (e.g. the download endpoint) should read the blob directly
 * rather than materialising it via {@link #toBytes(AttachmentEntity)}; that helper exists for the legacy decode and for
 * callers that genuinely need a {@code byte[]} (e.g. building a multipart file).
 *
 * <p>
 * The {@code sha256Hex} methods compute the SHA-256 content hash identically on every code path (the live upload in
 * {@code AttachmentService} and the base64-to-binary backfill job): the lower-case hex encoding of the SHA-256 digest
 * over the file's raw bytes, which matches MariaDB's {@code lower(sha2(content, 256))} (verified by
 * {@code AttachmentHashConsistencyTest}) so the application- and database-side hashes are interchangeable.
 */
public final class AttachmentContents {

	private static final String HASH_ALGORITHM = "SHA-256";

	private AttachmentContents() {}

	/**
	 * Computes the SHA-256 hash (lower-case hex) over the supplied bytes.
	 */
	public static String sha256Hex(final byte[] content) {
		return HexFormat.of().formatHex(newDigest().digest(content));
	}

	/**
	 * Computes the SHA-256 hash (lower-case hex) by streaming the supplied input through a {@link DigestInputStream}
	 * without materialising it in memory. The stream is fully consumed and closed.
	 */
	public static String sha256Hex(final InputStream content) throws IOException {
		final var digest = newDigest();
		try (final var in = new DigestInputStream(content, digest)) {
			in.transferTo(OutputStream.nullOutputStream());
		}
		return HexFormat.of().formatHex(digest.digest());
	}

	private static MessageDigest newDigest() {
		try {
			return MessageDigest.getInstance(HASH_ALGORITHM);
		} catch (final NoSuchAlgorithmException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s algorithm is not available: %s".formatted(HASH_ALGORITHM, e.getMessage()));
		}
	}

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
