package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;

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

import se.sundsvall.casedata.integration.db.AttachmentRepository;
import se.sundsvall.casedata.integration.db.DecisionRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.NoteRepository;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Attachment;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Facility;
import se.sundsvall.casedata.integration.db.model.Note;
import se.sundsvall.casedata.integration.db.model.Stakeholder;

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
	void testFindFacilityHistoryNothingFound() {
		when(facilityRepositoryMock.findByIdAndMunicipalityId(123L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> historyService.findFacilityHistory(123L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Facility not found");

		verify(facilityRepositoryMock).findByIdAndMunicipalityId(123L, MUNICIPALITY_ID);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindHistoryFacilityFound() {
		when(facilityRepositoryMock.findByIdAndMunicipalityId(123L, MUNICIPALITY_ID)).thenReturn(Optional.of(mock(Facility.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		// call the findHistory() method with an existing facility id
		final String jsonChanges = historyService.findFacilityHistory(123L, MUNICIPALITY_ID);

		// assert that the returned JSON string is not empty
		assertThat(jsonChanges).isNotEmpty();

		// verify that the findChanges() method of the Javers dependency was called with the expected QueryBuilder argument
		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindAttachmentHistory() {
		when(attachmentRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(mock(Attachment.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findAttachmentHistory(1L, MUNICIPALITY_ID);
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindAttachmentHistoryNothingFound() {
		when(attachmentRepositoryMock.findByIdAndMunicipalityId(123L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> historyService.findAttachmentHistory(123L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Attachment not found");

		verify(attachmentRepositoryMock).findByIdAndMunicipalityId(123L, MUNICIPALITY_ID);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindDecisionHistory() {
		when(decisionRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(mock(Decision.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findDecisionHistory(1L, MUNICIPALITY_ID);
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindDecisionHistoryNothingFound() {
		when(decisionRepositoryMock.findByIdAndMunicipalityId(123L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> historyService.findDecisionHistory(123L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Decision not found");


		verify(decisionRepositoryMock).findByIdAndMunicipalityId(123L, MUNICIPALITY_ID);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindErrandHistory() {
		when(errandRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(mock(Errand.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findErrandHistory(1L, MUNICIPALITY_ID);
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindErrandHistoryNothingFound() {
		when(errandRepositoryMock.findByIdAndMunicipalityId(123L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> historyService.findErrandHistory(123L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Errand not found");

		verify(errandRepositoryMock).findByIdAndMunicipalityId(123L, MUNICIPALITY_ID);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindNoteHistory() {
		when(noteRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(mock(Note.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findNoteHistory(1L, MUNICIPALITY_ID);
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindNoteHistoryNothingFound() {
		when(noteRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> historyService.findNoteHistory(1L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Note not found");

		verify(noteRepositoryMock).findByIdAndMunicipalityId(1L, MUNICIPALITY_ID);
		verifyNoInteractions(javers);
	}

	@Test
	void testFindHistoryStakeholderFound() {
		when(stakeholderRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(mock(Stakeholder.class)));
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findStakeholderHistory(1L, MUNICIPALITY_ID);
		assertThat(jsonChanges).isNotEmpty();

		verify(javers).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindHistoryNothingFound() {
		when(stakeholderRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> historyService.findStakeholderHistory(1L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND)
			.hasFieldOrPropertyWithValue("detail", "Stakeholder not found");

		verify(stakeholderRepositoryMock).findByIdAndMunicipalityId(1L, MUNICIPALITY_ID);
		verifyNoInteractions(javers);
	}
}
