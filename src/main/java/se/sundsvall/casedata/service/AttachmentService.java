package se.sundsvall.casedata.service;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import java.util.List;

import static java.text.MessageFormat.format;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ERRAND_WAS_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAttachment;

@Service
@Transactional
public class AttachmentService {

	private static final String ATTACHMENT_NOT_FOUND = "Attachment not found";

	private final AttachmentRepository attachmentRepository;

	private final ErrandRepository errandRepository;

	public AttachmentService(final AttachmentRepository attachmentRepository, final ErrandRepository errandRepository) {
		this.attachmentRepository = attachmentRepository;
		this.errandRepository = errandRepository;
	}

	public Attachment findByIdAndMunicipalityIdAndNamespace(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);
		return toAttachment(getAttachmentByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace));
	}

	public List<Attachment> findByErrandNumberAndMunicipalityIdAndNamespace(final String errandNumber, final String municipalityId, final String namespace) {
		return attachmentRepository.findAllByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, municipalityId, namespace).stream()
			.map(EntityMapper::toAttachment)
			.toList();
	}

	@Retry(name = "OptimisticLocking")
	public AttachmentEntity createAttachment(final Attachment attachment, final String municipalityId, final String namespace) {
		final var attachmentEntity = toAttachmentEntity(attachment, municipalityId, namespace);
		return attachmentRepository.save(attachmentEntity);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceAttachment(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		verifyErrandExists(errandId, municipalityId, namespace);

		final var attachmentEntity = getAttachmentByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace);
		attachmentRepository.save(putAttachment(attachmentEntity, attachment));
	}

	@Retry(name = "OptimisticLocking")
	public void updateAttachment(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		verifyErrandExists(errandId, municipalityId, namespace);

		final var attachmentEntity = getAttachmentByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace);
		attachmentRepository.save(patchAttachment(attachmentEntity, attachment));
	}

	@Retry(name = "OptimisticLocking")
	public boolean deleteAttachment(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace) {
		verifyErrandExists(errandId, municipalityId, namespace);

		if (attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace)) {
			attachmentRepository.deleteByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace);
			return true;
		}
		return false;
	}

	private AttachmentEntity getAttachmentByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace) {
		return attachmentRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, ATTACHMENT_NOT_FOUND));
	}

	private void verifyErrandExists(final Long errandId, final String municipalityId, final String namespace) {
		if (!errandRepository.existsByIdAndMunicipalityIdAndNamespace(errandId, municipalityId, namespace)) {
			throw Problem.valueOf(NOT_FOUND, format(ERRAND_WAS_NOT_FOUND, errandId));
		}
	}

}
