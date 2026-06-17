package se.sundsvall.casedata.service.util;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Base64;
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
}
