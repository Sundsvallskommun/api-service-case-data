package se.sundsvall.casedata.service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.NotificationSubType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ATTACHMENT_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ATTACHMENT_CREATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ATTACHMENT_DELETED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_ATTACHMENT_UPDATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_UPDATE_TYPE;
import static se.sundsvall.casedata.service.util.ResponseStreamer.streamAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;

/**
 * Attachments belonging directly to an errand. Attachments owned by one of the errand's decisions are handled by
 * {@link DecisionAttachmentService} and are never reached through these operations, since they carry no errand id.
 */
@Service
@Transactional
public class AttachmentService {

	private final AttachmentRepository attachmentRepository;
	private final NotificationService notificationService;
	private final ErrandRepository errandRepository;
	private final AttachmentContentWriter contentWriter;

	public AttachmentService(final AttachmentRepository attachmentRepository, final NotificationService notificationService, final ErrandRepository errandRepository, final AttachmentContentWriter contentWriter) {
		this.attachmentRepository = attachmentRepository;
		this.notificationService = notificationService;
		this.errandRepository = errandRepository;
		this.contentWriter = contentWriter;
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

		streamAttachment(response, attachmentEntity, attachmentId);
	}

	public AttachmentEntity create(final Long errandId, final Attachment attachment, final MultipartFile file, final String municipalityId, final String namespace) {
		// Verify the errand exists before materialising the upload, so a request to a missing errand fails fast and cheaply.
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId));
		}

		final var attachmentEntity = toAttachmentEntity(errandId, attachment, municipalityId, namespace);
		contentWriter.applyContent(attachmentEntity, file);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_ATTACHMENT_CREATED, NotificationSubType.ATTACHMENT), errandEntity);
		return attachmentRepository.save(attachmentEntity);
	}

	public void update(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_ATTACHMENT_UPDATED, NotificationSubType.ATTACHMENT), errandEntity);
		attachmentRepository.save(patchAttachment(attachmentEntity, attachment));
	}

	public void delete(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_ATTACHMENT_DELETED, NotificationSubType.ATTACHMENT), errandEntity);
		attachmentRepository.delete(attachmentEntity);
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
