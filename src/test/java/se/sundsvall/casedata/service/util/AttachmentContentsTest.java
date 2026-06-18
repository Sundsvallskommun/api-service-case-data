package se.sundsvall.casedata.service.util;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
import java.util.HexFormat;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

class AttachmentContentsTest {

	@Test
	void toBytesPrefersBinaryContent() throws SQLException {
		final var content = "binary content".getBytes(UTF_8);
		final var entity = AttachmentEntity.builder()
			.withContent(new SerialBlob(content))
			.withFile("should-be-ignored")
			.build();

		assertThat(AttachmentContents.toBytes(entity)).isEqualTo(content);
	}

	@Test
	void toBytesFallsBackToLegacyBase64() {
		final var content = "legacy content".getBytes(UTF_8);
		final var entity = AttachmentEntity.builder()
			.withFile(Base64.getEncoder().encodeToString(content))
			.build();

		assertThat(AttachmentContents.toBytes(entity)).isEqualTo(content);
	}

	@Test
	void toBytesWithNoContentAtAll() {
		assertThat(AttachmentContents.toBytes(AttachmentEntity.builder().build())).isEmpty();
	}

	@Test
	void toBytesWrapsBlobReadFailure() throws SQLException {
		final var blob = mock(Blob.class);
		when(blob.getBinaryStream()).thenThrow(new SQLException("boom"));
		final var entity = AttachmentEntity.builder().withId(42L).withContent(blob).build();

		assertThatThrownBy(() -> AttachmentContents.toBytes(entity))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR)
			.hasMessageContaining("attachment with id '42'");
	}

	@Test
	void decodeBase64WithNullOrBlankYieldsEmpty() {
		assertThat(AttachmentContents.decodeBase64(null, 1L)).isEmpty();
		assertThat(AttachmentContents.decodeBase64("   ", 1L)).isEmpty();
	}

	@Test
	void decodeBase64WithMalformedContentThrows() {
		assertThatThrownBy(() -> AttachmentContents.decodeBase64("not valid base64 @@@", 7L))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR)
			.hasMessageContaining("Attachment with id '7' has malformed base64 content");
	}

	private static String referenceHash(final byte[] content) throws Exception {
		return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
	}

	@Test
	void sha256HexFromBytesMatchesReferenceDigest() throws Exception {
		// Genuinely binary content (text plus embedded NUL/0xFF/control bytes).
		final var text = "Bilaga med abc 123 och text".getBytes(UTF_8);
		final var content = new byte[text.length + 4];
		System.arraycopy(text, 0, content, 0, text.length);
		content[text.length] = 0x00;
		content[text.length + 1] = 0x01;
		content[text.length + 2] = (byte) 0xFF;
		content[text.length + 3] = 0x02;

		assertThat(AttachmentContents.sha256Hex(content))
			.isEqualTo(referenceHash(content))
			.hasSize(64)
			.matches("[0-9a-f]{64}");
	}

	@Test
	void sha256HexFromStreamMatchesByteOverload() throws Exception {
		final var content = "streamed content with åäö".getBytes(UTF_8);

		assertThat(AttachmentContents.sha256Hex(new ByteArrayInputStream(content)))
			.isEqualTo(AttachmentContents.sha256Hex(content))
			.isEqualTo(referenceHash(content));
	}

	@Test
	void sha256HexOfEmptyContent() throws Exception {
		assertThat(AttachmentContents.sha256Hex(new byte[0]))
			.isEqualTo(AttachmentContents.sha256Hex(new ByteArrayInputStream(new byte[0])))
			.isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
	}
}
