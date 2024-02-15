package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.service.NoteService.ERRAND_NOT_FOUND_PROBLEM;
import static se.sundsvall.casedata.service.NoteService.NOTE_CANNOT_BE_DELETED_PROBLEM;
import static se.sundsvall.casedata.service.NoteService.NOTE_NOT_FOUND_PROBLEM;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.api.model.NoteDTO;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

	@Mock
	private NoteRepository noteRepository;

	@Mock
	private ErrandRepository errandRepository;

	@Mock
	private ProcessService processService;

	@InjectMocks
	private NoteService noteService;

	@Captor
	private ArgumentCaptor<Note> noteCaptor;

	@Test
	void testPatchNoteOnErrand() throws JsonProcessingException {
		final Errand errand = toErrand(createErrandDTO());
		errand.setId(new Random().nextLong(1, 1000));
		final Note note = toNote(createNoteDTO());
		note.setId(new Random().nextLong());
		errand.setNotes(List.of(note));

		final var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
		mockNote.setErrand(errand);
		doReturn(Optional.of(mockNote)).when(noteRepository).findById(note.getId());

		final NoteDTO patch = new NoteDTO();
		patch.setTitle(RandomStringUtils.random(10, true, false));
		patch.setText(RandomStringUtils.random(10, true, false));
		patch.setExtraParameters(createExtraParameters());
		patch.setCreatedBy(RandomStringUtils.random(10, true, false));
		patch.setUpdatedBy(RandomStringUtils.random(10, true, false));

		noteService.updateNote(note.getId(), patch);
		Mockito.verify(noteRepository).save(noteCaptor.capture());
		final Note persistedNote = noteCaptor.getValue();

		assertEquals(patch.getTitle(), persistedNote.getTitle());
		assertEquals(patch.getText(), persistedNote.getText());
		assertEquals(patch.getCreatedBy(), persistedNote.getCreatedBy());
		assertEquals(patch.getUpdatedBy(), persistedNote.getUpdatedBy());

		// ExtraParameters should contain all objects
		final Map<String, Object> extraParams = new HashMap<>();
		extraParams.putAll(patch.getExtraParameters());
		extraParams.putAll(note.getExtraParameters());
		assertEquals(extraParams, persistedNote.getExtraParameters());
	}

	@Test
	void testPatchNoteNotFound() {
		final NoteDTO noteDTO = new NoteDTO();
		final var problem = assertThrows(ThrowableProblem.class, () -> noteService.updateNote(1L, noteDTO));

		assertEquals(Status.NOT_FOUND, problem.getStatus());
	}

	@Test
	void testPut() throws JsonProcessingException {

		final Errand errand = toErrand(createErrandDTO());
		errand.setId(new Random().nextLong(1, 1000));
		final Note note = toNote(createNoteDTO());
		note.setId(new Random().nextLong());
		errand.setNotes(List.of(note));

		final var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
		mockNote.setErrand(errand);
		doReturn(Optional.of(mockNote)).when(noteRepository).findById(note.getId());

		final NoteDTO putDTO = new NoteDTO();
		putDTO.setTitle(RandomStringUtils.random(10, true, false));
		putDTO.setText(RandomStringUtils.random(10, true, false));
		putDTO.setNoteType(NoteType.PUBLIC);

		noteService.replaceNote(note.getId(), putDTO);
		Mockito.verify(noteRepository).save(noteCaptor.capture());
		final Note persistedNote = noteCaptor.getValue();

		assertThat(putDTO)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes(
				"id", "version", "created", "updated")
			.isEqualTo(toNoteDto(persistedNote));
	}

	@Test
	void testPatch() {
		final var dto = new NoteDTO();
		final var entity = new Note();
		entity.setErrand(new Errand());
		when(noteRepository.findById(1L)).thenReturn(Optional.of(entity));

		noteService.updateNote(1L, dto);

		verify(noteRepository, times(1)).save(entity);
		verifyNoMoreInteractions(noteRepository);
	}

	@Test
	void testGetNotesByErrandIdAndNoteType() {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepository.findById(any(Long.class))).thenReturn(Optional.of(errand));

		var result = noteService.getNotesByErrandIdAndNoteType(errand.getId(), Optional.of(NoteType.PUBLIC));

		assertThat(result).isNotEmpty().hasSize(1);
	}

	@Test
	void getNotesByErrandIdAndNoteType_EmptyOptional_Test() {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepository.findById(any(Long.class))).thenReturn(Optional.of(errand));

		var result = noteService.getNotesByErrandIdAndNoteType(errand.getId(), Optional.empty());

		assertThat(result).isNotEmpty().hasSize(2);
	}

	@Test
	void getNotesByErrandIdAndNoteType_NotFound_Test() {
		final var noteType = Optional.of(NoteType.PUBLIC);
		when(errandRepository.findById(3213L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noteService.getNotesByErrandIdAndNoteType(3213L, noteType))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", ERRAND_NOT_FOUND_PROBLEM.getStatus())
			.hasFieldOrPropertyWithValue("detail", ERRAND_NOT_FOUND_PROBLEM.getDetail());
	}

	@ParameterizedTest
	@EnumSource(NoteType.class)
	void getNotesByErrandIdAndNoteType_PublicNoteType_Test(final NoteType enumValue) {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		final var noteType = Optional.of(enumValue);
		when(errandRepository.findById(3213L)).thenReturn(Optional.of(errand));

		var result = noteService.getNotesByErrandIdAndNoteType(3213L, noteType);

		assertThat(result).isNotEmpty().hasSize(1).allSatisfy(n -> assertThat(n.getNoteType()).isEqualTo(enumValue));
		verify(errandRepository).findById(3213L);
	}

	@Test
	void deleteNoteById_PublicNote_Test() {
		final var note = createNote();
		note.setNoteType(NoteType.PUBLIC);
		when(noteRepository.findById(any(Long.class))).thenReturn(Optional.of(note));

		assertThatThrownBy(() -> noteService.deleteNoteById(5L))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOTE_CANNOT_BE_DELETED_PROBLEM.getStatus())
			.hasFieldOrPropertyWithValue("detail", NOTE_CANNOT_BE_DELETED_PROBLEM.getDetail());

		verify(noteRepository, times(1)).findById(any(Long.class));
		verify(noteRepository, never()).delete(any(Note.class));
	}

	@Test
	void deleteNoteById_InternalNote_Test() {
		final var note = createNote();
		note.setNoteType(NoteType.INTERNAL);
		when(noteRepository.findById(123L)).thenReturn(Optional.of(note));

		noteService.deleteNoteById(123L);

		verify(noteRepository, times(1)).findById(123L);
		verify(noteRepository, times(1)).delete(note);
	}

	@Test
	void deleteNoteById_NotFound_Test() {
		when(noteRepository.findById(123L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noteService.deleteNoteById(123L))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOTE_NOT_FOUND_PROBLEM.getStatus())
			.hasFieldOrPropertyWithValue("detail", NOTE_NOT_FOUND_PROBLEM.getDetail());

		verify(noteRepository).findById(123L);
		verify(noteRepository, never()).delete(any(Note.class));
	}

}
