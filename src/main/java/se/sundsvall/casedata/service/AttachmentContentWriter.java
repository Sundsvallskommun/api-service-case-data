package se.sundsvall.casedata.service;

import java.io.IOException;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.AttachmentContents;
import se.sundsvall.casedata.service.util.BlobBuilder;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Writes the binary content of an uploaded attachment onto its entity, shared by the errand and decision attachment
 * services so that both store content identically during the base64-to-binary migration.
 */
@Component
public class AttachmentContentWriter {

	private final BlobBuilder blobBuilder;

	@Value("${attachment.storage.write-mode:DUAL}")
	private AttachmentStorageMode writeMode = AttachmentStorageMode.DUAL;

	public AttachmentContentWriter(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	/**
	 * Persists the uploaded content according to the configured {@link AttachmentStorageMode}: as a base64 string in the
	 * legacy {@code file} column ({@code BASE64}/{@code DUAL}) and/or as a binary blob in {@code content} together with a
	 * SHA-256 (hex) {@code hash} ({@code DUAL}/{@code BLOB}). An empty or missing upload leaves all columns unset.
	 *
	 * <p>
	 * In the {@code BLOB} end state the content is streamed (the hash via a {@code DigestInputStream}, the blob lazily from
	 * the upload) so the whole file is never held in memory. The {@code DUAL}/{@code BASE64} modes must read the bytes once
	 * to produce the legacy base64 column and reuse them for the blob and hash.
	 */
	public void applyContent(final AttachmentEntity attachmentEntity, final MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return;
		}
		if (writeMode.writesBase64()) {
			final var content = readBytes(file);
			attachmentEntity.setFile(encode(content));
			if (writeMode.writesBlob()) {
				attachmentEntity.setContent(blobBuilder.createBlob(content));
				attachmentEntity.setHash(AttachmentContents.sha256Hex(content));
			}
		} else if (writeMode.writesBlob()) {
			applyStreamedBlob(attachmentEntity, file);
		}
	}

	/**
	 * Streams the upload into the {@code content} blob and computes its SHA-256 hash without materialising the whole file
	 * in memory. The upload is read twice from its (temp-file backed) source: once through a {@code DigestInputStream} to
	 * compute the hash and once lazily by the JDBC driver when the blob is flushed.
	 */
	private void applyStreamedBlob(final AttachmentEntity attachmentEntity, final MultipartFile file) {
		try {
			attachmentEntity.setHash(AttachmentContents.sha256Hex(file.getInputStream()));
			attachmentEntity.setContent(blobBuilder.createBlob(file.getInputStream(), file.getSize()));
		} catch (final IOException e) {
			throw Problem.valueOf(BAD_REQUEST, "%s occurred when reading uploaded file: %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
		}
	}

	private static String encode(final byte[] content) {
		return Base64.getEncoder().encodeToString(content);
	}

	private static byte[] readBytes(final MultipartFile file) {
		try {
			return file.getBytes();
		} catch (final IOException e) {
			throw Problem.valueOf(BAD_REQUEST, "%s occurred when reading uploaded file: %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
		}
	}
}
