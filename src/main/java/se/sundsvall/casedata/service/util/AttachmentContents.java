package se.sundsvall.casedata.service.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.HexFormat;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Single source of truth for an attachment's raw bytes and their content hash.
 *
 * <p>
 * Content is stored as a binary blob in the {@code attachment.content} column. Callers that can stream (e.g. the
 * download endpoint) should read the blob directly rather than materialising it via {@link #toBytes(AttachmentEntity)};
 * that helper exists for callers that genuinely need a {@code byte[]} (e.g. building a multipart file).
 *
 * <p>
 * The {@code sha256Hex} methods compute the SHA-256 content hash identically on every code path: the lower-case hex
 * encoding of the SHA-256 digest over the file's raw bytes, which matches MariaDB's {@code lower(sha2(content, 256))}
 * (verified by {@code AttachmentHashConsistencyTest}) so the application- and database-side hashes are interchangeable.
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
	 * Returns the raw bytes of the attachment, materialising the {@code content} blob in memory. An attachment stored
	 * without content yields an empty array.
	 */
	public static byte[] toBytes(final AttachmentEntity attachment) {
		final var blob = attachment.getContent();
		if (blob == null) {
			return new byte[0];
		}
		return readBlob(blob, attachment.getId());
	}

	private static byte[] readBlob(final Blob blob, final Object attachmentId) {
		try (final var in = blob.getBinaryStream()) {
			return in.readAllBytes();
		} catch (final IOException | SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s occurred when reading content of attachment with id '%s': %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
		}
	}
}
