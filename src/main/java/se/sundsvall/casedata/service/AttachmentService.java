package se.sundsvall.casedata.service;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.ATTACHMENT_ENTITY_NOT_FOUND;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;
import static se.sundsvall.casedata.service.util.mappers.PatchMapper.patchAttachment;
import static se.sundsvall.casedata.service.util.mappers.PutMapper.putAttachment;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import org.jose4j.base64url.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@Service
@Transactional
public class AttachmentService {

	private final AttachmentRepository attachmentRepository;

	public AttachmentService(final AttachmentRepository attachmentRepository) {
		this.attachmentRepository = attachmentRepository;
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
	public AttachmentEntity create(final Long errandId, final Attachment attachment, final String municipalityId, final String namespace, final MultipartFile file) {
		final var attachmentEntity = toAttachmentEntity(errandId, attachment, municipalityId, namespace);
		try {
			attachmentEntity.setFile(Base64.encode(file.getBytes()));
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to encode file: %s".formatted(e.getMessage()));
		}
		return attachmentRepository.save(attachmentEntity);
	}

	@Retry(name = "OptimisticLocking")
	public void replace(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		attachmentRepository.save(putAttachment(attachmentEntity, attachment));
	}

	@Retry(name = "OptimisticLocking")
	public void update(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace, final Attachment attachment) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		attachmentRepository.save(patchAttachment(attachmentEntity, attachment));
	}

	@Retry(name = "OptimisticLocking")
	public void delete(final Long errandId, final Long attachmentId, final String municipalityId, final String namespace) {
		final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);
		attachmentRepository.delete(attachmentEntity);
	}

	public void findAttachmentAsStreamedResponse(final Long attachmentId, final Long errandId, final String municipalityId, final String namespace, final HttpServletResponse response) {
		try {
			final var attachmentEntity = findAttachmentEntity(attachmentId, errandId, municipalityId, namespace);

			final var file = Base64.decode(attachmentEntity.getFile());

			response.addHeader(CONTENT_TYPE, attachmentEntity.getMimeType());
			response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachmentEntity.getName() + "\"");
			response.setContentLength(file.length);
			StreamUtils.copy(file, response.getOutputStream());
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "%s occurred when copying file with attachment id '%s' to response: %s".formatted(e.getClass().getSimpleName(), attachmentId, e.getMessage()));
		}
	}

	private AttachmentEntity findAttachmentEntity(final Long id, final Long errandId, final String municipalityId, final String namespace) {
		return attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, municipalityId, namespace)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, ATTACHMENT_ENTITY_NOT_FOUND.formatted(id, errandId, namespace, municipalityId)));
	}

}
