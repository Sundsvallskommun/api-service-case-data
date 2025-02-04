package se.sundsvall.casedata.service;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentEntity;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

	@Mock
	private ErrandRepository errandRepository;

	@Mock
	private AttachmentRepository attachmentRepository;

	@InjectMocks
	private AttachmentService attachmentService;

	@Captor
	private ArgumentCaptor<AttachmentEntity> attachmentArgumentCaptor;

	@Test
	void findAttachment() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var attachment = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.SIGNATURE), MUNICIPALITY_ID, NAMESPACE);
		when(attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachment));

		// Act
		final var result = attachmentService.findAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEqualTo(toAttachment(attachment));
		verify(attachmentRepository).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void findAttachmentNotFound() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		when(attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> attachmentService.findAttachment(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
		verify(attachmentRepository).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void replace() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var attachmentEntity = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.PASSPORT_PHOTO), MUNICIPALITY_ID, NAMESPACE);
		attachmentEntity.setId(attachmentId);
		final var attachment = createAttachment(AttachmentCategory.LEASE_REQUEST);
		when(attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));

		// Act
		attachmentService.replace(errandId, attachmentEntity.getId(), MUNICIPALITY_ID, NAMESPACE, attachment);

		// Assert
		verify(attachmentRepository).save(attachmentArgumentCaptor.capture());
		assertThat(attachmentArgumentCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getExtraParameters()).isEqualTo(attachment.getExtraParameters());
			assertThat(entity.getCategory()).isEqualTo(attachment.getCategory());
			assertThat(entity.getName()).isEqualTo(attachment.getName());
			assertThat(entity.getNote()).isEqualTo(attachment.getNote());
			assertThat(entity.getExtension()).isEqualTo(attachment.getExtension());
			assertThat(entity.getMimeType()).isEqualTo(attachment.getMimeType());
			assertThat(entity.getFile()).isEqualTo(attachment.getFile());
		});
	}

	@Test
	void update() {

		// Arrange
		final var errandId = 1L;
		final var attachmentId = 123L;
		final var dto = new Attachment();
		final var entity = new AttachmentEntity();

		when(attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		// Act
		attachmentService.update(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(attachmentRepository).save(entity);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void delete() {

		// Arrange
		final var attachmentId = 1L;
		final var errandId = 2L;
		final var attachmentEntity = TestUtil.createAttachmentEntity();

		when(attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(attachmentEntity));

		// Act
		attachmentService.delete(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(attachmentRepository).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepository).delete(attachmentEntity);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void deleteNotFound() {

		// Arrange
		final var attachmentId = 1L;
		final var errandId = 2L;
		when(attachmentRepository.findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var result = assertThrows(ThrowableProblem.class, () -> attachmentService.delete(errandId, attachmentId, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(result)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment with id:'1' not found on errand with id:'2' in namespace:'my.namespace' for municipality with id:'2281'");

		verify(attachmentRepository).findByIdAndErrandIdAndMunicipalityIdAndNamespace(attachmentId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepository, never()).delete(any(AttachmentEntity.class));
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void findAttachments() {
		// Arrange
		final var errandId = 123L;
		final var attachment = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.MEX_PROTOCOL), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandId(errandId);
		doReturn(List.of(attachment)).when(attachmentRepository).findAllByErrandIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		// Act
		final var result = attachmentService.findAttachments(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(List.of(toAttachment(attachment)), result);
		verify(attachmentRepository).findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void findAttachmentsNothingFound() {
		// Arrange
		final var errandId = 123L;
		doReturn(List.of()).when(attachmentRepository).findAllByErrandIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		// Act
		final var result = attachmentService.findAttachments(errandId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(List.of(), result);
		verify(attachmentRepository).findAllByErrandIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void create() {
		// Arrange
		final var errandId = 123L;
		final var attachment = toAttachmentEntity(errandId, createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandId(errandId);
		doReturn(attachment).when(attachmentRepository).save(any(AttachmentEntity.class));

		// Act
		final var result = attachmentService.create(errandId, createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(attachment, result);
		verify(attachmentRepository).save(any(AttachmentEntity.class));
		verifyNoMoreInteractions(attachmentRepository);
	}
}
