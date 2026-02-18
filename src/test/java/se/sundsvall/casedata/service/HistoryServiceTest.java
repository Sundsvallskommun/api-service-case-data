package se.sundsvall.casedata.service;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import org.javers.common.string.PrettyValuePrinter;
import org.javers.core.Changes;
import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.json.JsonConverter;
import org.javers.repository.jql.JqlQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.history.History;
import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.AttachmentEntity;
import se.sundsvall.casedata.integration.db.model.DecisionEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.FacilityEntity;
import se.sundsvall.casedata.integration.db.model.NoteEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

	@Mock
	private JsonConverter jsonConverter;

	@Mock
	private Javers javers;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private DecisionRepository decisionRepositoryMock;

	@Mock
	private AttachmentRepository attachmentRepositoryMock;

	@Mock
	private FacilityRepository facilityRepositoryMock;

	@Mock
	private NoteRepository noteRepositoryMock;

	@Mock
	private StakeholderRepository stakeholderRepositoryMock;

	@InjectMocks
	private HistoryService historyService;

	@Test
	void findFacilityHistoryOnErrandNothingFound() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(facilityRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findFacilityHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Facility not found");

		verify(facilityRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void findFacilityHistoryOnErrand() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(facilityRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(FacilityEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));

		// Act
		final var jsonChanges = historyService.findFacilityHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();
		verify(facilityRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void findAttachmentHistoryOnErrand() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(AttachmentEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));

		// Act
		final var jsonChanges = historyService.findAttachmentHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();
		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void findAttachmentHistoryOnErrandNothingFound() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(attachmentRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findAttachmentHistoryOnErrand(1L, 123L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment not found");

		verify(attachmentRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void findDecisionHistoryOnErrand() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(DecisionEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));

		// Act
		final var jsonChanges = historyService.findDecisionHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();
		verify(decisionRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void findDecisionHistoryOnErrandNothingFound() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(decisionRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findDecisionHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Decision not found");

		verify(decisionRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void findErrandHistory() {
		// Arrange
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(ErrandEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));
		// Act
		final var jsonChanges = historyService.findErrandHistory(1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void findErrandHistoryNothingFound() {
		// Arrange
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findErrandHistory(123L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Errand not found");

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void findNoteHistoryOnErrand() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(noteRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(NoteEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));

		// Act
		final var jsonChanges = historyService.findNoteHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();

		verify(noteRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void findNoteHistoryOnErrandNothingFound() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(noteRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findNoteHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note not found");

		verify(noteRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void findStakeholderHistoryOnErrand() {
		// Arrange
		final var id = 123L;
		final var errandId = 1L;
		when(stakeholderRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(StakeholderEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));
		// Act
		final var jsonChanges = historyService.findStakeholderHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void findStakeholderHistoryOnErrandNothingFound() {
		final var id = 123L;
		final var errandId = 1L;
		when(stakeholderRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findStakeholderHistoryOnErrand(errandId, id, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Stakeholder not found");

		verify(stakeholderRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(id, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}
}
