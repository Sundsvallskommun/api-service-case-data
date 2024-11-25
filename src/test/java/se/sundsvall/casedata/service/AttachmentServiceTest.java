package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
	void testFindById() {
		// Arrange
		final var attachment = toAttachmentEntity(createAttachment(AttachmentCategory.SIGNATURE), MUNICIPALITY_ID, NAMESPACE);
		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(attachment));
		when(errandRepository.existsByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act
		final var result = attachmentService.findByIdAndMunicipalityIdAndNamespace(1L, 5L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEqualTo(toAttachment(attachment));
		verify(attachmentRepository).findByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void testFindByIdNotFound() {
		// Arrange
		when(errandRepository.existsByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> attachmentService.findByIdAndMunicipalityIdAndNamespace(1L, 5L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);
		verify(attachmentRepository).findByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void putAttachment() {
		// Arrange
		final var attachmentEntity = toAttachmentEntity(createAttachment(AttachmentCategory.PASSPORT_PHOTO), MUNICIPALITY_ID, NAMESPACE);
		attachmentEntity.setId(2L);
		final var attachment = createAttachment(AttachmentCategory.LEASE_REQUEST);
		when(errandRepository.existsByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(attachmentEntity));

		// Act
		attachmentService.replaceAttachment(1L, attachmentEntity.getId(), MUNICIPALITY_ID, NAMESPACE, attachment);

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
	void testPatch() {
		// Arrange
		final var dto = new Attachment();
		final var entity = new AttachmentEntity();
		when(errandRepository.existsByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		// Act
		attachmentService.updateAttachment(1L, 1L, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(attachmentRepository).save(entity);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testDelete() {
		// Arrange
		when(errandRepository.existsByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);

		// Act
		attachmentService.deleteAttachment(1L, 1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(attachmentRepository).existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepository).deleteByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testDeleteNotFound() {
		// Arrange
		when(errandRepository.existsByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		when(attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(false);

		// Act
		attachmentService.deleteAttachment(1L, 1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(attachmentRepository).existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepository, never()).deleteByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testFindByErrandNumberAndMunicipalityId() {
		// Arrange
		final var attachment = toAttachmentEntity(createAttachment(AttachmentCategory.MEX_PROTOCOL), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandNumber("someErrandNumber");
		doReturn(List.of(attachment)).when(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		// Act
		final var result = attachmentService.findByErrandNumberAndMunicipalityIdAndNamespace("someErrandNumber", MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(List.of(toAttachment(attachment)), result);
		verify(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testFindByErrandNumberAndMunicipalityIdNothingFound() {
		// Arrange
		doReturn(List.of()).when(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		// Act
		final var result = attachmentService.findByErrandNumberAndMunicipalityIdAndNamespace("someErrandNumber", MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(List.of(), result);
		verify(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testPost() {
		// Arrange
		final var attachment = toAttachmentEntity(createAttachment(AttachmentCategory.POWER_OF_ATTORNEY), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandNumber("someErrandNumber");
		doReturn(attachment).when(attachmentRepository).save(any(AttachmentEntity.class));

		// Act
		final var result = attachmentService.createAttachment(createAttachment(AttachmentCategory.ROAD_ALLOWANCE_APPROVAL), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertEquals(attachment, result);
		verify(attachmentRepository).save(any(AttachmentEntity.class));
		verifyNoMoreInteractions(attachmentRepository);
	}

}
