package se.sundsvall.casedata.service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.Note;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.enums.NoteType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

	private static final Random RANDOM = new Random();

	@Mock
	private NoteRepository noteRepositoryMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ApplicationEventPublisher applicationEventPublisherMock;

	@Mock
	private NotificationService notificationServiceMock;

	@InjectMocks
	private NoteService noteService;

	@Captor
	private ArgumentCaptor<NoteEntity> noteCaptor;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	@Test
	void updateNotFound() {
		// Arrange
		final var note = new Note();
		final var errandId = 1L;
		final var noteId = 2L;
		final var errand = new ErrandEntity();
		errand.setNotes(null);
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act & Assert
		assertThatThrownBy(() -> noteService.update(errandId, noteId, MUNICIPALITY_ID, NAMESPACE, note))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note with id:'%s' was not found on errand with id:'%s'".formatted(noteId, errandId));

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(notificationServiceMock);
	}

	@Test
	void update() {
		// Arrange
		final var errandId = 1L;
		final var noteId = 1L;
		final var note = new Note();
		final var entity = new NoteEntity();
		final var errand = new ErrandEntity();
		entity.setId(noteId);
		entity.setErrand(errand);
		errand.setNotes(List.of(entity));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		noteService.update(errandId, noteId, MUNICIPALITY_ID, NAMESPACE, note);

		// Assert
		verify(noteRepositoryMock).saveAndFlush(entity);
		verifyNoMoreInteractions(noteRepositoryMock);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(errand));
		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Notering uppdaterad");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@Test
	void findNotes() {
		// Arrange
		final var errand = createErrandEntity();
		final var list = List.of(TestUtil.createNoteEntity(c -> c.setNoteType(NoteType.INTERNAL)), TestUtil.createNoteEntity(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		final var result = noteService.findNotes(errand.getId(), MUNICIPALITY_ID, NAMESPACE, Optional.of(NoteType.PUBLIC));

		// Assert
		assertThat(result).isNotEmpty().hasSize(1);
	}

	@Test
	void findNotesEmptyOptional() {
		// Arrange
		final var errand = createErrandEntity();
		final var list = List.of(TestUtil.createNoteEntity(c -> c.setNoteType(NoteType.INTERNAL)), TestUtil.createNoteEntity(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		final var result = noteService.findNotes(errand.getId(), MUNICIPALITY_ID, NAMESPACE, Optional.empty());

		// Assert
		assertThat(result).isNotEmpty().hasSize(2);
	}

	@Test
	void findNotesNotFound() {
		// Arrange
		final var noteId = 3213L;
		final var noteType = Optional.of(NoteType.PUBLIC);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(noteId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> noteService.findNotes(noteId, MUNICIPALITY_ID, NAMESPACE, noteType))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Errand with id:'%s' not found in namespace:'%s' for municipality with id:'%s'".formatted(noteId, NAMESPACE, MUNICIPALITY_ID));
	}

	@ParameterizedTest
	@EnumSource(NoteType.class)
	void findNotesParameterized(final NoteType enumValue) {
		// Arrange
		final var errand = createErrandEntity();
		final var list = List.of(TestUtil.createNoteEntity(c -> c.setNoteType(NoteType.INTERNAL)), TestUtil.createNoteEntity(c -> c.setNoteType(NoteType.PUBLIC)));
		errand.setNotes(list);
		final var noteType = Optional.of(enumValue);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(3213L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		final var result = noteService.findNotes(3213L, MUNICIPALITY_ID, NAMESPACE, noteType);

		// Assert
		assertThat(result).isNotEmpty().hasSize(1).allSatisfy(n -> assertThat(n.getNoteType()).isEqualTo(enumValue));
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(3213L, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void delete() {
		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setCaseType("MEX_LAND_RIGHT");
		// Set ID on every note
		errand.getNotes().forEach(note -> note.setId(new Random().nextLong()));

		final var errandId = RANDOM.nextLong(1, 1000);
		final var note = errand.getNotes().getFirst();
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		noteService.delete(errandId, MUNICIPALITY_ID, NAMESPACE, note.getId());

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(errand);
		verify(applicationEventPublisherMock).publishEvent(errand);
		verifyNoMoreInteractions(errandRepositoryMock, applicationEventPublisherMock);
	}

	@Test
	void addNote() {
		// Arrange
		final var errand = createErrandEntity();
		final var newNote = Note.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withTitle(RandomStringUtils.secure().next(10, true, false))
			.withText(RandomStringUtils.secure().next(10, true, false))
			.withExtraParameters(createExtraParameters())
			.withNoteType(NoteType.PUBLIC)
			.build();

		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var note = noteService.addNote(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newNote);

		// Assert
		assertThat(note).isEqualTo(newNote);
		assertThat(errand.getNotes()).isNotEmpty().hasSize(2);
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(errand);
		verify(applicationEventPublisherMock).publishEvent(errand);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(errand));
		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Notering skapad");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("CREATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
		verifyNoMoreInteractions(errandRepositoryMock, applicationEventPublisherMock, notificationServiceMock);
	}
}
