package se.sundsvall.casedata.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.NotificationSubType;
import se.sundsvall.casedata.service.util.BlobBuilder;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.dept44.problem.Problem;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ATTACHMENT_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.ResponseStreamer.streamBlob;
import static se.sundsvall.casedata.service.util.ResponseStreamer.streamBytes;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;

@Service
@Transactional
public class AttachmentService {

	private static final String NOTIFICATION_UPDATE_TYPE = "UPDATE";
	private static final String NOTIFICATION_ADD_ATTACHMENT = "En bilaga har lagts till i ärendet.";
	private static final String NOTIFICATION_UPDATE_ATTACHMENT = "En bilaga har uppdaterats i ärendet.";
	private static final String NOTIFICATION_REMOVE_ATTACHMENT = "En bilaga har tagits bort från ärendet.";
	private static final String HASH_ALGORITHM = "SHA-256";
	private final AttachmentRepository attachmentRepository;

	private final NotificationService notificationService;
	private final ErrandRepository errandRepository;
	private final BlobBuilder blobBuilder;

	@Value("${attachment.storage.write-mode:DUAL}")
	private AttachmentStorageMode writeMode = AttachmentStorageMode.DUAL;

	public AttachmentService(final AttachmentRepository attachmentRepository, final NotificationService notificationService, final ErrandRepository errandRepository, final BlobBuilder blobBuilder) {
		this.attachmentRepository = attachmentRepository;
		this.notificationService = notificationService;
		this.errandRepository = errandRepository;
		this.blobBuilder = blobBuilder;
	}

	public List<Attachment> findAttachments(final Long errandId, final String municipalityId, final String namespace) {
		return attachmentRepository.findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).stream()
			.map(EntityMapper::toAttachment)
			.toList();
	}

	/**
	 * Streams the raw binary content of an attachment to the supplied response.
	 *
	 * <p>
	 * New attachments are stored as a binary blob in the {@code attachment.content} column. Rows created before the
	 * binary-storage migration still hold their content base64-encoded in the legacy {@code attachment.file} column; for
	 * those the content is decoded on the fly. This fallback is removed once all rows have been migrated and the legacy
	 * column dropped.
	 */
	public void findAttachmentAsStreamedResponse(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final HttpServletResponse response) {
		final var attachmentEntity = attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ATTACHMENT_ENTITY_NOT_FOUND.formatted(attachmentId, errandId, namespace, municipalityId)));

		final var blob = attachmentEntity.getContent();
		if (blob != null) {
			streamBlob(response, attachmentEntity.getName(), attachmentEntity.getMimeType(), blob, attachmentId);
		} else {
			streamBytes(response, attachmentEntity.getName(), attachmentEntity.getMimeType(), decode(attachmentEntity.getFile(), attachmentId), attachmentId);
		}
	}

	public AttachmentEntity create(final Long errandId, final Attachment attachment, final MultipartFile file, final String municipalityId, final String namespace) {
		final var attachmentEntity = toAttachmentEntity(errandId, attachment, municipalityId, namespace);
		applyContent(attachmentEntity, file);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_ADD_ATTACHMENT, NotificationSubType.ATTACHMENT), errandEntity);
		return attachmentRepository.save(attachmentEntity);
	}

	public void update(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_UPDATE_ATTACHMENT, NotificationSubType.ATTACHMENT), errandEntity);
		attachmentRepository.save(patchAttachment(attachmentEntity, attachment));
	}

	public void delete(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_REMOVE_ATTACHMENT, NotificationSubType.ATTACHMENT), errandEntity);
		attachmentRepository.delete(attachmentEntity);
	}

	/**
	 * Persists the uploaded content according to the configured {@link AttachmentStorageMode}: as a base64 string in the
	 * legacy {@code file} column ({@code BASE64}/{@code DUAL}) and/or as a binary blob in {@code content} together with a
	 * SHA-256 (hex) {@code hash} ({@code DUAL}/{@code BLOB}). An empty or missing upload leaves all columns unset.
	 */
	private void applyContent(final AttachmentEntity attachmentEntity, final MultipartFile file) {
		final var content = readBytes(file);
		if (content.length == 0) {
			return;
		}
		if (writeMode.writesBase64()) {
			attachmentEntity.setFile(encode(content));
		}
		if (writeMode.writesBlob()) {
			attachmentEntity.setContent(blobBuilder.createBlob(content));
			attachmentEntity.setHash(computeHash(content));
		}
	}

	private static String encode(final byte[] content) {
		return Base64.getEncoder().encodeToString(content);
	}

	private byte[] readBytes(final MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return new byte[0];
		}
		try {
			return file.getBytes();
		} catch (final IOException e) {
			throw Problem.valueOf(BAD_REQUEST, "%s occurred when reading uploaded file: %s".formatted(e.getClass().getSimpleName(), e.getMessage()));
		}
	}

	private static String computeHash(final byte[] content) {
		try {
			final var digest = MessageDigest.getInstance(HASH_ALGORITHM);
			return HexFormat.of().formatHex(digest.digest(content));
		} catch (final NoSuchAlgorithmException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s algorithm is not available: %s".formatted(HASH_ALGORITHM, e.getMessage()));
		}
	}

	private byte[] decode(final String base64Content, final Long attachmentId) {
		if (base64Content == null || base64Content.isBlank()) {
			return new byte[0];
		}
		try {
			return Base64.getDecoder().decode(base64Content.getBytes(UTF_8));
		} catch (final IllegalArgumentException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Attachment with id '%s' has malformed base64 content and cannot be streamed: %s".formatted(attachmentId, e.getMessage()));
		}
	}

	private AttachmentEntity findAttachmentEntity(final Long id, final Long errandId, final String municipalityId, final String namespace) {
		return attachmentRepository.findWithPessimisticLockingByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ATTACHMENT_ENTITY_NOT_FOUND.formatted(id, errandId, namespace, municipalityId)));
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}
}
