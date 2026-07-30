package se.sundsvall.casedata.apptest;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static se.sundsvall.casedata.apptest.util.TestConstants.MUNICIPALITY_ID;
import static se.sundsvall.casedata.apptest.util.TestConstants.NAMESPACE;
import static se.sundsvall.dept44.support.Identifier.HEADER_NAME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import se.sundsvall.casedata.Application;
import se.sundsvall.dept44.test.AbstractAppTest;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;

/**
 * Exercises the end-state storage mode of the base64-to-binary migration (DRAKEN-4446), where an upload is written
 * only as a binary blob and its SHA-256 hash - the legacy base64 {@code file} column is left untouched.
 *
 * <p>
 * This mode takes a different code path than the default {@code DUAL} mode covered by {@link AttachmentIT}: the
 * content is never materialised as a {@code byte[]}, it is streamed twice from the (temp-file backed) upload, once
 * through a {@code DigestInputStream} for the hash and once lazily by the JDBC driver when the blob is flushed. That
 * double read cannot be proven with a mocked {@code MultipartFile}, which is why it is verified here against a real
 * servlet container and a real database.
 */
@WireMockAppTestSuite(files = "classpath:/AttachmentBlobWriteModeIT", classes = Application.class)
@TestPropertySource(properties = "attachment.storage.write-mode=BLOB")
@Sql({
	"/db/scripts/truncate.sql",
	"/db/scripts/attachmentIT-testdata.sql"
})
class AttachmentBlobWriteModeIT extends AbstractAppTest {

	private static final Long ERRAND_ID = 4L;
	private static final String ATTACHMENTS_PATH = "/{0}/{1}/errands/{2}/attachments";
	private static final String ADMIN_IDENTIFIER = "type=adAccount; user123";

	// Precomputed SHA-256 of the shared test image, the same value the test data SQL stores for the fixture rows.
	private static final String TEST_IMAGE_HASH = "429e40fd4fee7d2533ebef54d5d442c8f12adb10b63fe5c7a81cc78914c6f795";
	private static final String TEST_IMAGE_RESOURCE = "/AttachmentBlobWriteModeIT/__files/test01_createAttachmentWritesBlobOnly/test_image.png";

	// Large enough that the digest is fed in many chunks rather than a single read, but deliberately below the test
	// database's max_allowed_packet (1 MiB by default in the Testcontainers image). A blob write that exceeds that
	// limit does not fail fast - the driver blocks and the request hangs until the client times out - so raising this
	// without raising max_allowed_packet turns the test into a three minute stall rather than a clear failure.
	private static final int LARGE_UPLOAD_SIZE = 768 * 1024;

	@TempDir
	private Path tempDir;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void test01_createAttachmentWritesBlobOnly() throws IOException {
		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", "test_image.png")
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		// The content survives the streamed write and comes back binary-identical.
		setupCall()
			.withHttpMethod(GET)
			.withServicePath(location)
			.withExpectedResponseStatus(OK)
			.withExpectedResponseHeader(CONTENT_TYPE, List.of(IMAGE_PNG_VALUE))
			.withExpectedBinaryResponse("test_image.png")
			.sendRequest();

		assertStoredAsBlobOnly(attachmentIdFrom(location), sizeOfClasspathFile(TEST_IMAGE_RESOURCE), TEST_IMAGE_HASH);
	}

	@Test
	void test02_createLargeAttachmentHashesEveryChunk() throws IOException, NoSuchAlgorithmException {
		// A deterministic multi-megabyte upload: if the digest were computed over anything but the complete content
		// (a truncated first read, a re-read of an already consumed stream), the hashes below would diverge.
		final var content = deterministicContent(LARGE_UPLOAD_SIZE);
		final var upload = Files.write(tempDir.resolve("large-upload.bin"), content);

		final var location = setupCall()
			.withHttpMethod(POST)
			.withServicePath(format(ATTACHMENTS_PATH, MUNICIPALITY_ID, NAMESPACE, ERRAND_ID))
			.withContentType(MULTIPART_FORM_DATA)
			.withRequestFile("attachment", "attachment.json")
			.withRequestFile("file", upload.toFile())
			.withHeader(HEADER_NAME, ADMIN_IDENTIFIER)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseBodyIsNull()
			.sendRequest()
			.getResponseHeaders().get(LOCATION).getFirst();

		assertStoredAsBlobOnly(attachmentIdFrom(location), content.length, sha256Hex(content));
	}

	/**
	 * Asserts that the row holds the content as a binary blob only, and that the hash the application computed while
	 * streaming the upload describes exactly the bytes that ended up in the database. The hash is cross-checked
	 * against MariaDB's own digest of the stored blob, so a silently truncated or re-encoded write cannot pass.
	 */
	private void assertStoredAsBlobOnly(final long attachmentId, final long expectedSize, final String expectedHash) {
		final var row = jdbcTemplate.queryForMap("""
			select file, octet_length(content) as content_length, hash, lower(sha2(content, 256)) as database_hash
			from attachment
			where id = ?
			""", attachmentId);

		assertThat(row.get("file")).as("the legacy base64 column must not be written in BLOB mode").isNull();
		assertThat(((Number) row.get("content_length")).longValue()).as("stored blob size").isEqualTo(expectedSize);
		assertThat(row.get("hash")).as("hash computed by the application").isEqualTo(expectedHash);
		assertThat(row.get("database_hash")).as("hash computed by MariaDB over the stored blob").isEqualTo(expectedHash);
	}

	private long attachmentIdFrom(final String location) {
		return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
	}

	private long sizeOfClasspathFile(final String resource) throws IOException {
		try (final var stream = getClass().getResourceAsStream(resource)) {
			assertThat(stream).as("test fixture %s must exist", resource).isNotNull();
			return stream.readAllBytes().length;
		}
	}

	private static byte[] deterministicContent(final int size) {
		final var content = new byte[size];
		new Random(20260730L).nextBytes(content);
		return content;
	}

	private static String sha256Hex(final byte[] content) throws NoSuchAlgorithmException {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
	}
}
