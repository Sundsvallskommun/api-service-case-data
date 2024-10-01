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
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentDto;

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

import se.sundsvall.casedata.api.model.AttachmentDTO;
import se.sundsvall.casedata.api.model.validation.enums.AttachmentCategory;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

	@Mock
	private AttachmentRepository attachmentRepository;

	@InjectMocks
	private AttachmentService attachmentService;

	@Captor
	private ArgumentCaptor<Attachment> attachmentArgumentCaptor;

	@Test
	void testFindById() {
		final var attachment = toAttachment(createAttachmentDTO(AttachmentCategory.SIGNATURE), MUNICIPALITY_ID, NAMESPACE);
		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(attachment));

		final var result = attachmentService.findByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE);
		assertThat(result).isEqualTo(toAttachmentDto(attachment));

		verify(attachmentRepository).findByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void testFindByIdNotFound() {
		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> attachmentService.findByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);

		verify(attachmentRepository).findByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void putAttachment() {
		final Attachment attachment = toAttachment(createAttachmentDTO(AttachmentCategory.PASSPORT_PHOTO), MUNICIPALITY_ID, NAMESPACE);
		final AttachmentDTO attachmentDTO = createAttachmentDTO(AttachmentCategory.ARCHAEOLOGICAL_ASSESSMENT);

		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(attachment));

		attachmentService.replaceAttachment(attachment.getId(), MUNICIPALITY_ID, NAMESPACE, attachmentDTO);

		verify(attachmentRepository).save(attachmentArgumentCaptor.capture());

		assertThat(attachmentArgumentCaptor.getValue()).satisfies(entity -> {
			assertThat(entity.getExtraParameters()).isEqualTo(attachmentDTO.getExtraParameters());
			assertThat(entity.getCategory()).isEqualTo(attachmentDTO.getCategory());
			assertThat(entity.getName()).isEqualTo(attachmentDTO.getName());
			assertThat(entity.getNote()).isEqualTo(attachmentDTO.getNote());
			assertThat(entity.getExtension()).isEqualTo(attachmentDTO.getExtension());
			assertThat(entity.getMimeType()).isEqualTo(attachmentDTO.getMimeType());
			assertThat(entity.getFile()).isEqualTo(attachmentDTO.getFile());
		});
	}

	@Test
	void testPatch() {
		final var dto = new AttachmentDTO();
		final var entity = new Attachment();
		when(attachmentRepository.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		attachmentService.updateAttachment(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		verify(attachmentRepository).save(entity);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testDelete() {

		when(attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(true);
		attachmentService.deleteAttachment(1L, MUNICIPALITY_ID, NAMESPACE);

		verify(attachmentRepository).existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepository).deleteByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testDeleteNotFound() {
		when(attachmentRepository.existsByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(false);

		attachmentService.deleteAttachment(1L, MUNICIPALITY_ID, NAMESPACE);

		verify(attachmentRepository).existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(attachmentRepository, never()).deleteByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testFindByErrandNumberAndMunicipalityId() {

		final var attachment = toAttachment(createAttachmentDTO(AttachmentCategory.ARCHAEOLOGICAL_ASSESSMENT), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandNumber("someErrandNumber");
		doReturn(List.of(attachment)).when(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		final var result = attachmentService.findByErrandNumberAndMunicipalityId("someErrandNumber", MUNICIPALITY_ID, NAMESPACE);
		assertEquals(List.of(toAttachmentDto(attachment)), result);

		verify(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testFindByErrandNumberAndMunicipalityIdNothingFound() {

		doReturn(List.of()).when(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		final var result = attachmentService.findByErrandNumberAndMunicipalityId("someErrandNumber", MUNICIPALITY_ID, NAMESPACE);
		assertEquals(List.of(), result);

		verify(attachmentRepository).findAllByErrandNumberAndMunicipalityIdAndNamespace(any(String.class), eq(MUNICIPALITY_ID), eq(NAMESPACE));

		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testPost() {

		final var attachment = toAttachment(createAttachmentDTO(AttachmentCategory.ANMALAN_ANDRING_AVLOPPSANLAGGNING), MUNICIPALITY_ID, NAMESPACE);
		attachment.setErrandNumber("someErrandNumber");
		doReturn(attachment).when(attachmentRepository).save(any(Attachment.class));

		final var result = attachmentService.createAttachment(createAttachmentDTO(AttachmentCategory.ADDRESS_SHEET), MUNICIPALITY_ID, NAMESPACE);
		assertEquals(attachment, result);

		verify(attachmentRepository).save(any(Attachment.class));

		verifyNoMoreInteractions(attachmentRepository);
	}

}
