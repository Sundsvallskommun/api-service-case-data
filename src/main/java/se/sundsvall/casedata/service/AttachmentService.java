package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ATTACHMENT_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.NOTIFICATION_SUBTYPE_ATTACHMENT;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNotification;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAttachment;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
@Transactional
public class AttachmentService {
	private static final String NOTIFICATION_UPDATE_TYPE = "UPDATE";
	private static final String NOTIFICATION_ADD_ATTACHMENT = "En bilaga har lagts till i ärendet.";
	private static final String NOTIFICATION_REPLACE_ATTACHMENT = "En bilaga har ersatts i ärendet.";
	private static final String NOTIFICATION_UPDATE_ATTACHMENT = "En bilaga har uppdaterats i ärendet.";
	private static final String NOTIFICATION_REMOVE_ATTACHMENT = "En bilaga har tagits bort från ärendet.";
	private final AttachmentRepository attachmentRepository;

	private final NotificationService notificationService;
	private final ErrandRepository errandRepository;

	public AttachmentService(final AttachmentRepository attachmentRepository, final NotificationService notificationService, final ErrandRepository errandRepository) {
		this.attachmentRepository = attachmentRepository;
		this.notificationService = notificationService;
		this.errandRepository = errandRepository;
	}

	public Attachment findAttachment(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace) {
		return toAttachment(findAttachmentEntity(attachmentId, errandId, municipalityId, namespace));
	}

	public List<Attachment> findAttachments(final Long errandId, final String municipalityId, final String namespace) {
		return attachmentRepository.findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace).stream()
			.map(EntityMapper::toAttachment)
			.toList();
	}

	@Retry(name = "OptimisticLocking")
	public AttachmentEntity create(final Long errandId, final Attachment attachment, final String municipalityId, final String namespace) {
		final var attachmentEntity = toAttachmentEntity(errandId, attachment, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(findErrandEntity(errandId, municipalityId, namespace), NOTIFICATION_UPDATE_TYPE, NOTIFICATION_ADD_ATTACHMENT, NOTIFICATION_SUBTYPE_ATTACHMENT));
		return attachmentRepository.save(attachmentEntity);
	}

	@Retry(name = "OptimisticLocking")
	public void replace(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(findErrandEntity(errandId, municipalityId, namespace), NOTIFICATION_UPDATE_TYPE, NOTIFICATION_REPLACE_ATTACHMENT, NOTIFICATION_SUBTYPE_ATTACHMENT));
		attachmentRepository.save(putAttachment(attachmentEntity, attachment));
	}

	@Retry(name = "OptimisticLocking")
	public void update(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(findErrandEntity(errandId, municipalityId, namespace), NOTIFICATION_UPDATE_TYPE, NOTIFICATION_UPDATE_ATTACHMENT, NOTIFICATION_SUBTYPE_ATTACHMENT));
		attachmentRepository.save(patchAttachment(attachmentEntity, attachment));
	}

	@Retry(name = "OptimisticLocking")
	public void delete(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		notificationService.create(municipalityId, namespace, toNotification(findErrandEntity(errandId, municipalityId, namespace), NOTIFICATION_UPDATE_TYPE, NOTIFICATION_REMOVE_ATTACHMENT, NOTIFICATION_SUBTYPE_ATTACHMENT));
		attachmentRepository.delete(attachmentEntity);
	}

	private AttachmentEntity findAttachmentEntity(final Long id, final Long errandId, final String municipalityId, final String namespace) {
		return attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ATTACHMENT_ENTITY_NOT_FOUND.formatted(id, errandId, namespace, municipalityId)));
	}

	private ErrandEntity findErrandEntity(final Long errandId, final String municipalityId, final String namespace) {
		return errandRepository.findByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ERRAND_ENTITY_NOT_FOUND.formatted(errandId, namespace, municipalityId)));
	}
}
