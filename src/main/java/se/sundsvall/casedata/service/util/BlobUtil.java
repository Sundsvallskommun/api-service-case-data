package se.sundsvall.casedata.service.util;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 * Single place where the binary content of a stored blob is materialised as a {@code byte[]}.
 *
 * <p>
 * Callers that can stream (e.g. the download endpoint, see {@link ResponseStreamer}) should read the blob directly
 * rather than materialising it here; this helper exists for callers that genuinely need a {@code byte[]} - building a
 * multipart file, or copying a message attachment onto the errand attachment that mirrors it.
 *
 * <p>
 * The SHA-256 content hash of the same bytes is computed by {@link se.sundsvall.dept44.util.HashUtils}.
 */
public final class BlobUtil {

	private BlobUtil() {}

	/**
	 * Returns the raw bytes of the supplied blob, materialising it in memory. A null blob yields an empty array. The
	 * {@code id} is only used to identify the owning attachment in the error message should the read fail.
	 */
	public static byte[] toBytes(final Blob blob, final Object id) {
		if (blob == null) {
			return new byte[0];
		}
		try (final var in = blob.getBinaryStream()) {
			return in.readAllBytes();
		} catch (final IOException | SQLException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s occurred when reading content of attachment with id '%s': %s".formatted(e.getClass().getSimpleName(), id, e.getMessage()));
		}
	}
}
