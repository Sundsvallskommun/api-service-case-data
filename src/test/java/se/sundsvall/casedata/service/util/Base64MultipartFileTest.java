package se.sundsvall.casedata.service.util;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Base64MultipartFileTest {

	@Test
	void constructorAndGetters() {
		// Arrange
		final var name = "attachments";
		final var originalFilename = "document.pdf";
		final var contentType = "application/pdf";
		final var content = "test content".getBytes(StandardCharsets.UTF_8);

		// Act
		final var file = new Base64MultipartFile(name, originalFilename, contentType, content);

		// Assert
		assertThat(file.getName()).isEqualTo(name);
		assertThat(file.getOriginalFilename()).isEqualTo(originalFilename);
		assertThat(file.getContentType()).isEqualTo(contentType);
		assertThat(file.getBytes()).isEqualTo(content);
		assertThat(file.getSize()).isEqualTo(content.length);
		assertThat(file.isEmpty()).isFalse();
		assertThat(file.getInputStream()).hasBinaryContent(content);
	}

	@Test
	void constructorWithNullContent() {
		// Act
		final var file = new Base64MultipartFile("attachments", "empty.txt", "text/plain", null);

		// Assert
		assertThat(file.isEmpty()).isTrue();
		assertThat(file.getSize()).isZero();
		assertThat(file.getBytes()).isEmpty();
		assertThat(file.getInputStream()).hasBinaryContent(new byte[0]);
	}

	@Test
	void constructorWithEmptyContent() {
		// Act
		final var file = new Base64MultipartFile("attachments", "empty.txt", "text/plain", new byte[0]);

		// Assert
		assertThat(file.isEmpty()).isTrue();
		assertThat(file.getSize()).isZero();
	}

	@Test
	void transferToThrowsUnsupportedOperationException() {
		// Arrange
		final var file = new Base64MultipartFile("attachments", "file.txt", "text/plain", "data".getBytes());

		// Act & Assert
		assertThatThrownBy(() -> file.transferTo(new java.io.File("target")))
			.isInstanceOf(UnsupportedOperationException.class)
			.hasMessage("transferTo is not supported");
	}
}
