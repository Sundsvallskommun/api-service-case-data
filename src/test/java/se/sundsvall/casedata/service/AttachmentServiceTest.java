package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createAttachmentDTO;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachment;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toAttachmentDto;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
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
		final Long id = new Random().nextLong();
		final var attachment = toAttachment(createAttachmentDTO(AttachmentCategory.SIGNATURE));
		attachment.setErrandNumber("someErrandNumber");
		doReturn(Optional.of(attachment)).when(attachmentRepository).findById(id);

		final var result = attachmentService.findById(id);
		assertEquals(toAttachmentDto(attachment), result);

		verify(attachmentRepository, times(1)).findById(id);
	}

	@Test
	void testFindByIdNotFound() {
		final Long id = new Random().nextLong();
		doReturn(Optional.empty()).when(attachmentRepository).findById(id);

		final var problem = assertThrows(ThrowableProblem.class, () -> attachmentService.findById(id));

		assertEquals(Status.NOT_FOUND, problem.getStatus());
		verify(attachmentRepository, times(1)).findById(id);
	}

	@Test
	void putAttachment() throws JsonProcessingException {

		final Attachment attachment = toAttachment(createAttachmentDTO(AttachmentCategory.PASSPORT_PHOTO));
		attachment.setId(new Random().nextLong());

		final var mockAttachment = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(attachment), Attachment.class);
		doReturn(Optional.of(mockAttachment)).when(attachmentRepository).findById(any());

		final AttachmentDTO putDTO = createAttachmentDTO((AttachmentCategory.ARCHAEOLOGICAL_ASSESSMENT));

		attachmentService.replaceAttachment(attachment.getId(), putDTO);

		verify(attachmentRepository).save(attachmentArgumentCaptor.capture());

		final Attachment persistedAttachment = attachmentArgumentCaptor.getValue();

		assertThat(putDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				"id", "version", "created", "updated")
			.isEqualTo(toAttachmentDto(persistedAttachment));
	}

	@Test
	void testPatch() {
		final var dto = new AttachmentDTO();
		final var entity = new Attachment();
		when(attachmentRepository.findById(1L)).thenReturn(Optional.of(entity));

		attachmentService.updateAttachment(1L, dto);

		verify(attachmentRepository, times(1)).save(entity);
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testDelete() {

		when(attachmentRepository.existsById(any(Long.class))).thenReturn(true);
		attachmentService.deleteAttachment(1L);

		verify(attachmentRepository, times(1)).existsById(any(Long.class));
		verify(attachmentRepository, times(1)).deleteById(any(Long.class));

		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testDeleteNotFound() {
		when(attachmentRepository.existsById(any(Long.class))).thenReturn(false);

		attachmentService.deleteAttachment(1L);

		verify(attachmentRepository, times(1)).existsById(any(Long.class));
		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testFindByErrandNumber() {

		final var attachment = toAttachment(createAttachmentDTO(AttachmentCategory.ARCHAEOLOGICAL_ASSESSMENT));
		attachment.setErrandNumber("someErrandNumber");
		doReturn(List.of(attachment)).when(attachmentRepository).findAllByErrandNumber(any(String.class));

		final var result = attachmentService.findByErrandNumber("someErrandNumber");
		assertEquals(List.of(toAttachmentDto(attachment)), result);

		verify(attachmentRepository, times(1)).findAllByErrandNumber(any(String.class));

		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testFindByErrandNumberNothingFound() {

		doReturn(List.of()).when(attachmentRepository).findAllByErrandNumber(any(String.class));

		final var result = attachmentService.findByErrandNumber("someErrandNumber");
		assertEquals(List.of(), result);

		verify(attachmentRepository, times(1)).findAllByErrandNumber(any(String.class));

		verifyNoMoreInteractions(attachmentRepository);
	}

	@Test
	void testPost() {

		final var attachment = toAttachment(createAttachmentDTO(AttachmentCategory.ANMALAN_ANDRING_AVLOPPSANLAGGNING));
		attachment.setErrandNumber("someErrandNumber");
		doReturn(attachment).when(attachmentRepository).save(any(Attachment.class));

		final var result = attachmentService.createAttachment(createAttachmentDTO(AttachmentCategory.ADDRESS_SHEET));
		assertEquals(attachment, result);

		verify(attachmentRepository, times(1)).save(any(Attachment.class));

		verifyNoMoreInteractions(attachmentRepository);
	}

}
