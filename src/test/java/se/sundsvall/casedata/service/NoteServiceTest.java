package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.FORBIDDEN;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNote;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toNoteDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		final Errand errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		final Note note = toNote(createNoteDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		errand.setNotes(List.of(note));

		final var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
		mockNote.setErrand(errand);
		when(noteRepository.findByIdAndMunicipalityId(note.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(mockNote));

		final NoteDTO patch = new NoteDTO();
		patch.setTitle(RandomStringUtils.random(10, true, false));
		patch.setText(RandomStringUtils.random(10, true, false));
		patch.setExtraParameters(createExtraParameters());
		patch.setCreatedBy(RandomStringUtils.random(10, true, false));
		patch.setUpdatedBy(RandomStringUtils.random(10, true, false));

		noteService.updateNote(note.getId(), MUNICIPALITY_ID, patch);
		verify(noteRepository).save(noteCaptor.capture());
		final Note persistedNote = noteCaptor.getValue();

		assertThat(persistedNote.getTitle()).isEqualTo(patch.getTitle());
		assertThat(persistedNote.getText()).isEqualTo(patch.getText());
		assertThat(persistedNote.getCreatedBy()).isEqualTo(patch.getCreatedBy());
		assertThat(persistedNote.getUpdatedBy()).isEqualTo(patch.getUpdatedBy());

		// ExtraParameters should contain all objects
		final Map<String, Object> extraParams = new HashMap<>();
		extraParams.putAll(patch.getExtraParameters());
		extraParams.putAll(note.getExtraParameters());

		assertThat(persistedNote.getExtraParameters()).isEqualTo(extraParams);
	}

	@Test
	void testPatchNoteNotFound() {
		assertThatThrownBy(() -> noteService.updateNote(1L, MUNICIPALITY_ID, new NoteDTO()))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note was not found");
	}

	@Test
	void testPut() throws JsonProcessingException {
		final Errand errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		final Note note = toNote(createNoteDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		errand.setNotes(List.of(note));

		final var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
		mockNote.setErrand(errand);
		when(noteRepository.findByIdAndMunicipalityId(note.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(mockNote));

		final NoteDTO putDTO = new NoteDTO();
		putDTO.setMunicipalityId("2281");
		putDTO.setTitle(RandomStringUtils.random(10, true, false));
		putDTO.setText(RandomStringUtils.random(10, true, false));
		putDTO.setNoteType(NoteType.PUBLIC);

		noteService.replaceNote(note.getId(), MUNICIPALITY_ID, putDTO);
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
		when(noteRepository.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));

		noteService.updateNote(1L, MUNICIPALITY_ID, dto);

		verify(noteRepository).save(entity);
		verifyNoMoreInteractions(noteRepository);
	}

	@Test
	void testGetNotesByErrandIdAndMunicipalityIdAndNoteType() {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepository.findByIdAndMunicipalityId(any(Long.class), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(errand));

		final var result = noteService.getNotesByErrandIdAndMunicipalityIdAndNoteType(errand.getId(), MUNICIPALITY_ID, Optional.of(NoteType.PUBLIC));

		assertThat(result).isNotEmpty().hasSize(1);
	}

	@Test
	void getNotesByErrandIdAndMunicipalityIdAndNoteType_EmptyOptional_Test() {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepository.findByIdAndMunicipalityId(any(Long.class), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(errand));

		final var result = noteService.getNotesByErrandIdAndMunicipalityIdAndNoteType(errand.getId(), MUNICIPALITY_ID, Optional.empty());

		assertThat(result).isNotEmpty().hasSize(2);
	}

	@Test
	void getNotesByErrandIdAndMunicipalityIdAndNoteType_NotFound_Test() {
		final var noteType = Optional.of(NoteType.PUBLIC);
		when(errandRepository.findByIdAndMunicipalityId(3213L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noteService.getNotesByErrandIdAndMunicipalityIdAndNoteType(3213L, MUNICIPALITY_ID, noteType))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Errand was not found");
	}

	@ParameterizedTest
	@EnumSource(NoteType.class)
	void getNotesByErrandIdAndMunicipalityIdAndNoteType_PublicNoteType_Test(final NoteType enumValue) {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		final var noteType = Optional.of(enumValue);
		when(errandRepository.findByIdAndMunicipalityId(3213L, MUNICIPALITY_ID)).thenReturn(Optional.of(errand));

		final var result = noteService.getNotesByErrandIdAndMunicipalityIdAndNoteType(3213L, MUNICIPALITY_ID, noteType);

		assertThat(result).isNotEmpty().hasSize(1).allSatisfy(n -> assertThat(n.getNoteType()).isEqualTo(enumValue));
		verify(errandRepository).findByIdAndMunicipalityId(3213L, MUNICIPALITY_ID);
	}

	@Test
	void deleteNoteById_PublicNote_TestIdAndMunicipality() {
		final var note = createNote();
		note.setNoteType(NoteType.PUBLIC);
		when(noteRepository.findByIdAndMunicipalityId(any(Long.class), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(note));

		assertThatThrownBy(() -> noteService.deleteNoteByIdAndMunicipalityId(5L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", FORBIDDEN)
			.hasFieldOrPropertyWithValue("detail", "Public notes can not be deleted");

		verify(noteRepository).findByIdAndMunicipalityId(5L, MUNICIPALITY_ID);
		verify(noteRepository, never()).delete(any(Note.class));
	}

	@Test
	void deleteNoteById_InternalNote_TestIdAndMunicipality() {
		final var note = createNote();
		note.setNoteType(NoteType.INTERNAL);
		when(noteRepository.findByIdAndMunicipalityId(123L, MUNICIPALITY_ID)).thenReturn(Optional.of(note));

		noteService.deleteNoteByIdAndMunicipalityId(123L, MUNICIPALITY_ID);

		verify(noteRepository).findByIdAndMunicipalityId(123L, MUNICIPALITY_ID);
		verify(noteRepository).delete(note);
	}

	@Test
	void deleteNoteByIdAndMunicipalityId_NotFound_Test() {
		when(noteRepository.findByIdAndMunicipalityId(123L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noteService.deleteNoteByIdAndMunicipalityId(123L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note was not found");

		verify(noteRepository).findByIdAndMunicipalityId(123L, MUNICIPALITY_ID);
		verify(noteRepository, never()).delete(any(Note.class));
	}

}
