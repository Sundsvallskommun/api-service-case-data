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
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.OBJECT_MAPPER;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createNote;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.ANMALAN_ATTEFALL;
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
	private NoteRepository noteRepositoryMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ProcessService processServiceMock;

	@InjectMocks
	private NoteService noteService;

	@Captor
	private ArgumentCaptor<Note> noteCaptor;

	@Test
	void testPatchNoteOnErrand() throws JsonProcessingException {
		final Errand errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
		final Note note = toNote(createNoteDTO(), MUNICIPALITY_ID, NAMESPACE);
		errand.setNotes(List.of(note));

		final var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
		mockNote.setErrand(errand);
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(note.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mockNote));

		final NoteDTO patch = new NoteDTO();
		patch.setTitle(RandomStringUtils.secure().next(10, true, false));
		patch.setText(RandomStringUtils.secure().next(10, true, false));
		patch.setExtraParameters(createExtraParameters());
		patch.setCreatedBy(RandomStringUtils.secure().next(10, true, false));
		patch.setUpdatedBy(RandomStringUtils.secure().next(10, true, false));

		noteService.updateNote(note.getId(), MUNICIPALITY_ID, NAMESPACE, patch);
		verify(noteRepositoryMock).save(noteCaptor.capture());
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
		var dto = new NoteDTO();
		assertThatThrownBy(() -> noteService.updateNote(1L, MUNICIPALITY_ID, NAMESPACE, dto))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note was not found");
	}

	@Test
	void testPut() throws JsonProcessingException {
		final Errand errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
		final Note note = toNote(createNoteDTO(), MUNICIPALITY_ID, NAMESPACE);
		errand.setNotes(List.of(note));

		final var mockNote = OBJECT_MAPPER.readValue(OBJECT_MAPPER.writeValueAsString(note), Note.class);
		mockNote.setErrand(errand);
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(note.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mockNote));

		final NoteDTO putDTO = new NoteDTO();
		putDTO.setTitle(RandomStringUtils.secure().next(10, true, false));
		putDTO.setText(RandomStringUtils.secure().next(10, true, false));
		putDTO.setNoteType(NoteType.PUBLIC);

		noteService.replaceNote(note.getId(), MUNICIPALITY_ID, NAMESPACE, putDTO);
		Mockito.verify(noteRepositoryMock).save(noteCaptor.capture());
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
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		noteService.updateNote(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		verify(noteRepositoryMock).save(entity);
		verifyNoMoreInteractions(noteRepositoryMock);
	}

	@Test
	void testGetNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType() {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		final var result = noteService.getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType(errand.getId(), MUNICIPALITY_ID, NAMESPACE, Optional.of(NoteType.PUBLIC));

		assertThat(result).isNotEmpty().hasSize(1);
	}

	@Test
	void getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType_EmptyOptional_Test() {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		final var result = noteService.getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType(errand.getId(), MUNICIPALITY_ID, NAMESPACE, Optional.empty());

		assertThat(result).isNotEmpty().hasSize(2);
	}

	@Test
	void getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType_NotFound_Test() {
		final var noteType = Optional.of(NoteType.PUBLIC);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(3213L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noteService.getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType(3213L, MUNICIPALITY_ID, NAMESPACE, noteType))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Errand with id: 3,213 was not found");
	}

	@ParameterizedTest
	@EnumSource(NoteType.class)
	void getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType_PublicNoteType_Test(final NoteType enumValue) {
		final var errand = createErrand();
		final var list = List.of(createNote(c -> c.setNoteType(NoteType.INTERNAL)), createNote(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		final var noteType = Optional.of(enumValue);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(3213L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		final var result = noteService.getNotesByErrandIdAndMunicipalityIdAndNamespaceAndNoteType(3213L, MUNICIPALITY_ID, NAMESPACE, noteType);

		assertThat(result).isNotEmpty().hasSize(1).allSatisfy(n -> assertThat(n.getNoteType()).isEqualTo(enumValue));
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(3213L, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void deleteNoteById_PublicNote_TestIdAndMunicipality() {
		final var note = createNote();
		note.setNoteType(NoteType.PUBLIC);
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(note));

		assertThatThrownBy(() -> noteService.deleteNoteByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", FORBIDDEN)
			.hasFieldOrPropertyWithValue("detail", "Public notes can not be deleted");

		verify(noteRepositoryMock).findByIdAndMunicipalityIdAndNamespace(5L, MUNICIPALITY_ID, NAMESPACE);
		verify(noteRepositoryMock, never()).delete(any(Note.class));
	}

	@Test
	void deleteNoteById_InternalNote_TestIdAndMunicipality() {
		final var note = createNote();
		note.setNoteType(NoteType.INTERNAL);
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(note));

		noteService.deleteNoteByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);

		verify(noteRepositoryMock).findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);
		verify(noteRepositoryMock).delete(note);
	}

	@Test
	void deleteNoteByIdAndMunicipalityId_NotFound_Test() {
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> noteService.deleteNoteByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note was not found");

		verify(noteRepositoryMock).findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);
		verify(noteRepositoryMock, never()).delete(any(Note.class));
	}

	@Test
	void deleteNoteOnErrand() {
		// Arrange
		final var errand = toErrand(createErrandDTO(), MUNICIPALITY_ID, NAMESPACE);
		errand.setCaseType(ANMALAN_ATTEFALL.name());
		// Set ID on every note
		errand.getNotes().forEach(note -> note.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var note = errand.getNotes().getFirst();

		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		noteService.deleteNoteOnErrand(errandId, MUNICIPALITY_ID, NAMESPACE, note.getId());

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}


	@Test
	void addNoteToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newNote = createNoteDTO();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var note = noteService.addNoteToErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newNote);

		// Assert
		assertThat(note).isEqualTo(newNote);
		assertThat(errand.getNotes()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

}
