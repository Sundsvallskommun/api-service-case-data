package se.sundsvall.casedata.service;

import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.BlobBuilder;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.util.HashUtils;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Single place where the binary content of an attachment is written onto its entity: the raw bytes go into the
 * {@code content} blob and their SHA-256 (hex) digest into {@code hash}. Shared by every code path that creates an
 * attachment - the errand and decision attachment services as well as the email and web message collectors - so that
 * content is stored identically regardless of origin.
 */
@Component
public class AttachmentContentWriter {

	private final BlobBuilder blobBuilder;

	public AttachmentContentWriter(final BlobBuilder blobBuilder) {
		this.blobBuilder = blobBuilder;
	}

	/**
	 * Persists an uploaded file as the attachment's binary {@code content} together with its SHA-256 {@code hash}. An
	 * empty or missing upload leaves both columns unset.
	 *
	 * <p>
	 * The content is streamed rather than materialised, so the whole file is never held in memory: the upload is read
	 * twice from its (temp-file backed) source, once through a {@code DigestInputStream} to compute the hash and once
	 * lazily by the JDBC driver when the blob is flushed.
	 */
	public void applyContent(final AttachmentEntity attachmentEntity, final MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return;
		}
		try {
			attachmentEntity.setHash(HashUtils.sha256Hex(file.getInputStream()));
			attachmentEntity.setContent(blobBuilder.createBlob(file.getInputStream(), file.getSize()));
		} catch (final IOException e) {
			throw Problem.valueOf(BAD_REQUEST, "%s occurred when reading uploaded file: %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
		}
	}

	/**
	 * Persists already-materialised content as the attachment's binary {@code content} together with its SHA-256
	 * {@code hash}. Used by the collectors, which receive the content as a {@code byte[]} from the source system. A null
	 * or empty content leaves both columns unset.
	 */
	public void applyContent(final AttachmentEntity attachmentEntity, final byte[] content) {
		if (content == null || content.length == 0) {
			return;
		}
		attachmentEntity.setHash(HashUtils.sha256Hex(content));
		attachmentEntity.setContent(blobBuilder.createBlob(content));
	}
}
