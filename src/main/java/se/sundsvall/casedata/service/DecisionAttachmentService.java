package se.sundsvall.casedata.service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.NotificationSubType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.dept44.problem.Problem;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.DECISION_ATTACHMENT_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_DECISION_ATTACHMENT_CREATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_DECISION_ATTACHMENT_DELETED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_DECISION_ATTACHMENT_UPDATED;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_UPDATE_TYPE;
import static se.sundsvall.casedata.service.util.ResponseStreamer.streamAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;

/**
 * Attachments owned by a decision. They carry {@code attachment.decision_id} and no errand id, so they are never
 * returned by - or reachable through - the errand attachment operations in {@link AttachmentService}. Content is stored
 * exactly as for errand attachments.
 */
@Service
@Transactional
public class DecisionAttachmentService {

	private final AttachmentRepository attachmentRepository;
	private final DecisionRepository decisionRepository;
	private final ErrandRepository errandRepository;
	private final NotificationService notificationService;
	private final AttachmentContentWriter contentWriter;

	public DecisionAttachmentService(final AttachmentRepository attachmentRepository, final DecisionRepository decisionRepository, final ErrandRepository errandRepository, final NotificationService notificationService,
		final AttachmentContentWriter contentWriter) {
		this.attachmentRepository = attachmentRepository;
		this.decisionRepository = decisionRepository;
		this.errandRepository = errandRepository;
		this.notificationService = notificationService;
		this.contentWriter = contentWriter;
	}

	public List<Attachment> findAttachments(final Long errandId, final Long decisionId, final String municipalityId, final String namespace) {
		verifyDecisionOnErrand(errandId, decisionId, municipalityId, namespace);

		return attachmentRepository.findAllByDecisionIdAndMunicipalityIdAndNamespace(decisionId, municipalityId, namespace).stream()
			.map(EntityMapper::toAttachment)
			.toList();
	}

	/**
	 * Streams the raw binary content of a decision attachment to the supplied response. See
	 * {@link AttachmentService#findAttachmentAsStreamedResponse(Long, Long, String, String, HttpServletResponse)} for how
	 * the content is located.
	 */
	public void findAttachmentAsStreamedResponse(final Long errandId, final Long decisionId, final Long attachmentId, final String municipalityId, final String namespace, final HttpServletResponse response) {
		verifyDecisionOnErrand(errandId, decisionId, municipalityId, namespace);
		final var attachmentEntity = attachmentRepository.findByIdAndDecisionIdAndMunicipalityIdAndNamespace(attachmentId, decisionId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_ATTACHMENT_ENTITY_NOT_FOUND.formatted(attachmentId, decisionId, namespace, municipalityId)));

		streamAttachment(response, attachmentEntity, attachmentId);
	}

	public AttachmentEntity create(final Long errandId, final Long decisionId, final Attachment attachment, final MultipartFile file, final String municipalityId, final String namespace) {
		final var decisionEntity = findDecisionEntity(errandId, decisionId, municipalityId, namespace);

		// The attachment belongs to the decision only, hence no errand id.
		final var attachmentEntity = toAttachmentEntity(null, attachment, municipalityId, namespace);
		contentWriter.applyContent(attachmentEntity, file);

		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_DECISION_ATTACHMENT_CREATED, NotificationSubType.ATTACHMENT), errandEntity);

		// Saving through the attachment repository persists this very instance - so it gets the generated id the caller
		// needs - and lets JaVers record the attachment just like an errand attachment. Adding it to the decision is what
		// sets the decision_id foreign key, which the attachment itself only maps read-only, and the flush writes it.
		attachmentsOf(decisionEntity).add(attachmentEntity);
		attachmentRepository.save(attachmentEntity);
		decisionRepository.flush();

		return attachmentEntity;
	}

	public void update(final Long errandId, final Long decisionId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		verifyDecisionOnErrand(errandId, decisionId, municipalityId, namespace);
		final var attachmentEntity = findAttachmentEntity(attachmentId, decisionId, municipalityId, namespace);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_DECISION_ATTACHMENT_UPDATED, NotificationSubType.ATTACHMENT), errandEntity);
		attachmentRepository.save(patchAttachment(attachmentEntity, attachment));
	}

	public void delete(final Long errandId, final Long decisionId, final Long attachmentId, final String municipalityId, final String namespace) {
		final var decisionEntity = findDecisionEntity(errandId, decisionId, municipalityId, namespace);
		final var attachmentEntity = findAttachmentEntity(attachmentId, decisionId, municipalityId, namespace);
		final var errandEntity = findErrandEntity(errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(errandEntity, NOTIFICATION_UPDATE_TYPE, NOTIFICATION_DECISION_ATTACHMENT_DELETED, NotificationSubType.ATTACHMENT), errandEntity);

		// The attachment is removed from the decision to keep its in-memory collection consistent within the transaction,
		// and deleted through the attachment repository so that JaVers records the deletion just like for an errand
		// attachment. Matching on id rather than on the entity itself avoids the deep equals of AttachmentEntity, which
		// would touch the content blob.
		attachmentsOf(decisionEntity).removeIf(entity -> attachmentEntity.getId().equals(entity.getId()));
		attachmentRepository.delete(attachmentEntity);
	}

	private AttachmentEntity findAttachmentEntity(final Long id, final Long decisionId, final String municipalityId, final String namespace) {
		return attachmentRepository.findWithPessimisticLockingByIdAndDecisionIdAndMunicipalityIdAndNamespace(id, decisionId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_ATTACHMENT_ENTITY_NOT_FOUND.formatted(id, decisionId, namespace, municipalityId)));
	}

	private DecisionEntity findDecisionEntity(final Long errandId, final Long decisionId, final String municipalityId, final String namespace) {
		return decisionRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(decisionId, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, DECISION_WITH_ID_X_WAS_NOT_FOUND_ON_ERRAND_WITH_ID_X.formatted(decisionId, errandId)));
	}

	/**
	 * The attachment queries are scoped by decision id alone, so the decision has to be looked up to establish that it
	 * belongs to the errand in the path - otherwise an attachment could be reached through any errand id.
	 */
	private void verifyDecisionOnErrand(final Long errandId, final Long decisionId, final String municipalityId, final String namespace) {
		findDecisionEntity(errandId, decisionId, municipalityId, namespace);
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}

	private static List<AttachmentEntity> attachmentsOf(final DecisionEntity decisionEntity) {
		if (decisionEntity.getAttachments() == null) {
			decisionEntity.setAttachments(new ArrayList<>());
		}
		return decisionEntity.getAttachments();
	}
}
