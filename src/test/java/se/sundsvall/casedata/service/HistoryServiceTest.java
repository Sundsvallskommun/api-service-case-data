package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;

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
	void testFindFacilityHistoryOnErrandNothingFound() {
		// Arrange
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(facilityRepositoryMock.findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findFacilityHistoryOnErrand(1L, 123L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Facility not found");

		verify(facilityRepositoryMock).findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindHistoryFacilityFound() {
		// Arrange
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(facilityRepositoryMock.findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(FacilityEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));

		// Act
		final var jsonChanges = historyService.findFacilityHistoryOnErrand(1L, 123L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindAttachmentHistoryOnErrand() {
		// Arrange
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(AttachmentEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));

		// Act
		final var jsonChanges = historyService.findAttachmentHistoryOnErrand(1L, 1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindAttachmentHistoryOnErrandNothingFound() {
		// Arrange
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(attachmentRepositoryMock.findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findAttachmentHistoryOnErrand(1L, 123L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment not found");

		verify(attachmentRepositoryMock).findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindDecisionHistoryOnErrand() {
		// Arrange
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(decisionRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(DecisionEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));

		// Act
		final var jsonChanges = historyService.findDecisionHistoryOnErrand(1L, 1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindDecisionHistoryOnErrandNothingFound() {
		// Arrange
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(decisionRepositoryMock.findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findDecisionHistoryOnErrand(1L, 123L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Decision not found");

		verify(decisionRepositoryMock).findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindErrandHistory() {
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
	void testFindErrandHistoryNothingFound() {
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
	void testFindNoteHistoryOnErrand() {
		// Arrange
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(NoteEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));
		// Act
		final var jsonChanges = historyService.findNoteHistoryOnErrand(1L, 1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindNoteHistoryOnErrandNothingFound() {
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(noteRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());


		// Act & Assert
		assertThatThrownBy(() -> historyService.findNoteHistoryOnErrand(1L, 1L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note not found");

		verify(noteRepositoryMock).findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindHistoryStakeholderFound() {
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(stakeholderRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(mock(StakeholderEntity.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");
		when(jsonConverter.fromJson(eq("[]"), any(Type.class))).thenReturn(List.of(mock(History.class)));
		// Act
		final var jsonChanges = historyService.findStakeholderHistoryOnErrand(1L, 1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindHistoryNothingFound() {
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);
		when(stakeholderRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> historyService.findStakeholderHistoryOnErrand(1L, 1L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Stakeholder not found");

		verify(stakeholderRepositoryMock).findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoInteractions(javers);
	}

}
