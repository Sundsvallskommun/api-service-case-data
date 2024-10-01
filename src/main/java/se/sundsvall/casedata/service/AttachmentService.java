package se.sundsvall.casedata.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAttachment;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;

import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import io.github.resilience4j.retry.annotation.Retry;

@Service
@Transactional
public class AttachmentService {

	private static final String ATTACHMENT_NOT_FOUND = "Attachment not found";

	private final AttachmentRepository attachmentRepository;

	public AttachmentService(final AttachmentRepository attachmentRepository) {
		this.attachmentRepository = attachmentRepository;
	}

	public AttachmentDTO findByIdAndMunicipalityIdAndNamespace(final Long attachmentId, final String municipalityId, final String namespace) {
		return toAttachmentDto(getAttachmentByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace));
	}

	public List<AttachmentDTO> findByErrandNumberAndMunicipalityId(final String errandNumber, final String municipalityId, final String namespace) {
		return attachmentRepository.findAllByErrandNumberAndMunicipalityIdAndNamespace(errandNumber, municipalityId, namespace).stream()
			.map(EntityMapper::toAttachmentDto)
			.toList();
	}

	@Retry(name = "OptimisticLocking")
	public Attachment createAttachment(final AttachmentDTO attachmentDTO, final String municipalityId, final String namespace) {
		final var attachment = toAttachment(attachmentDTO, municipalityId, namespace);
		return attachmentRepository.save(attachment);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceAttachment(final Long attachmentId, final String municipalityId, final String namespace, final AttachmentDTO attachmentDTO) {
		final var attachment = getAttachmentByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace);
		attachmentRepository.save(putAttachment(attachment, attachmentDTO));
	}

	@Retry(name = "OptimisticLocking")
	public void updateAttachment(final Long attachmentId, final String municipalityId, final String namespace, final AttachmentDTO attachmentDTO) {
		final var attachment = getAttachmentByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace);
		attachmentRepository.save(patchAttachment(attachment, attachmentDTO));
	}

	@Retry(name = "OptimisticLocking")
	public boolean deleteAttachment(final Long attachmentId, final String municipalityId, final String namespace) {
		if (attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace)) {
			attachmentRepository.deleteByIdAndMunicipalityIdAndNamespace(attachmentId, municipalityId, namespace);
			return true;
		}
		return false;
	}

	private Attachment getAttachmentByIdAndMunicipalityIdAndNamespace(final Long id, final String municipalityId, final String namespace) {
		return attachmentRepository.findByIdAndMunicipalityIdAndNamespace(id, municipalityId, namespace).orElseThrow(() -> Problem.valueOf(NOT_FOUND, ATTACHMENT_NOT_FOUND));
	}

}
