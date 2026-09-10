package se.sundsvall.casedata.service.util;

import java.sql.Blob;
import java.sql.SQLException;
import javax.sql.rowset.serial.SerialBlob;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

class BlobUtilTest {

	@Test
	void toBytesReadsBinaryContent() throws SQLException {
		final var content = "binary content".getBytes(UTF_8);

		assertThat(BlobUtil.toBytes(new SerialBlob(content), 1L)).isEqualTo(content);
	}

	@Test
	void toBytesWithNoContentAtAll() {
		assertThat(BlobUtil.toBytes(null, 1L)).isEmpty();
	}

	@Test
	void toBytesOfEmptyBlob() throws SQLException {
		assertThat(BlobUtil.toBytes(new SerialBlob(new byte[1]), 1L)).hasSize(1);
	}

	@Test
	void toBytesWrapsBlobReadFailure() throws SQLException {
		final var blob = mock(Blob.class);
		when(blob.getBinaryStream()).thenThrow(new SQLException("boom"));

		assertThatThrownBy(() -> BlobUtil.toBytes(blob, 42L))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", INTERNAL_SERVER_ERROR)
			.hasMessageContaining("attachment with id '42'")
			.hasMessageContaining("boom");
	}
}
