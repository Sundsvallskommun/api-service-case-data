package se.sundsvall.casedata.service;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import java.util.List;

import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentDto;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAttachment;

@Service
@Transactional
public class AttachmentService {

	private static final String ATTACHMENT_NOT_FOUND = "Attachment not found";

	private final AttachmentRepository attachmentRepository;

	public AttachmentService(final AttachmentRepository attachmentRepository) {
		this.attachmentRepository = attachmentRepository;
	}

	public AttachmentDTO findById(final Long id) {
		return toAttachmentDto(getAttachmentById(id));
	}

	public List<AttachmentDTO> findByErrandNumber(final String errandNumber) {
		return attachmentRepository.findAllByErrandNumber(errandNumber).stream()
			.map(EntityMapper::toAttachmentDto)
			.toList();
	}

	@Retry(name = "OptimisticLocking")
	public Attachment createAttachment(final AttachmentDTO attachmentDTO) {
		final var attachment = toAttachment(attachmentDTO);
		return attachmentRepository.save(attachment);
	}

	@Retry(name = "OptimisticLocking")
	public void replaceAttachment(final Long attachmentId, final AttachmentDTO attachmentDTO) {
		final var attachment = getAttachmentById(attachmentId);
		attachmentRepository.save(putAttachment(attachment, attachmentDTO));
	}

	@Retry(name = "OptimisticLocking")
	public void updateAttachment(final Long attachmentId, final AttachmentDTO attachmentDTO) {
		final var attachment = getAttachmentById(attachmentId);
		attachmentRepository.save(patchAttachment(attachment, attachmentDTO));
	}

	@Retry(name = "OptimisticLocking")
	public boolean deleteAttachment(final Long attachmentId) {
		if (attachmentRepository.existsById(attachmentId)) {
			attachmentRepository.deleteById(attachmentId);
			return true;
		}
		return false;
	}

	private Attachment getAttachmentById(final Long id) {
		return attachmentRepository.findById(id).orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND, ATTACHMENT_NOT_FOUND));
	}
}
