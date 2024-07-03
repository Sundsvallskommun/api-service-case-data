package se.sundsvall.casedata.service;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAttachment;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

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

	public AttachmentDTO findByIdAndMunicipalityId(final Long attachmentId, final String municipalityId) {
		return toAttachmentDto(getAttachmentById(attachmentId, municipalityId));
	}

	public List<AttachmentDTO> findByErrandNumberAndMunicipalityId(final String errandNumber, final String municipalityId) {
		return attachmentRepository.findAllByErrandNumberAndMunicipalityId(errandNumber, municipalityId).stream()
			.map(EntityMapper::toAttachmentDto)
			.toList();
	}

	@Retry(name = "OptimisticLocking")
	public Attachment createAttachment(final AttachmentDTO attachmentDTO, final String municipalityId) {
		final var attachment = toAttachment(attachmentDTO, municipalityId);
		return attachmentRepository.save(attachment);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceAttachment(final Long attachmentId, final String municipalityId, final AttachmentDTO attachmentDTO) {
		final var attachment = getAttachmentById(attachmentId, municipalityId);
		attachmentRepository.save(putAttachment(attachment, attachmentDTO));
	}

	@Retry(name = "OptimisticLocking")
	public void updateAttachment(final Long attachmentId, final String municipalityId, final AttachmentDTO attachmentDTO) {
		final var attachment = getAttachmentById(attachmentId, municipalityId);
		attachmentRepository.save(patchAttachment(attachment, attachmentDTO));
	}

	@Retry(name = "OptimisticLocking")
	public boolean deleteAttachment(final Long attachmentId, final String municipalityId) {
		if (attachmentRepository.existsByIdAndMunicipalityId(attachmentId, municipalityId)) {
			attachmentRepository.deleteByIdAndMunicipalityId(attachmentId, municipalityId);
			return true;
		}
		return false;
	}

	private Attachment getAttachmentById(final Long id, final String municipalityId) {
		return attachmentRepository.findByIdAndMunicipalityId(id, municipalityId).orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, ATTACHMENT_NOT_FOUND));
	}
}
