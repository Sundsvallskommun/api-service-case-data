package se.sundsvall.casedata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

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

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

	@Mock
	private JsonConverter jsonConverter;

	@Mock
	private Javers javers;

	@InjectMocks
	private HistoryService historyService;

	@Test
	void testFindFacilityHistory() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(), PrettyValuePrinter.getDefault()));

		try {
			historyService.findFacilityHistory(123L);
		} catch (final ThrowableProblem e) {
			// assert that the expected exception is thrown
			assertEquals(Status.NOT_FOUND, e.getStatus());
			assertEquals("Facility not found", e.getDetail());
		}

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindHistoryFacilityFound() {
		// mock the findChanges() method of the Javers dependency to return a non-empty Changes object
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		// call the findHistory() method with an existing facility id
		final String jsonChanges = historyService.findFacilityHistory(123L);

		// verify that the findChanges() method of the Javers dependency was called with the expected QueryBuilder argument
		verify(javers).findChanges(any(JqlQuery.class));

		// assert that the returned JSON string is not empty
		assertFalse(jsonChanges.isEmpty());
	}

	@Test
	void testFindAttachmentHistory() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findAttachmentHistory(1L);
		assertFalse(jsonChanges.isEmpty());

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
	}

	@Test
	void testFindAttachmentHistoryNothingFound() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(), PrettyValuePrinter.getDefault()));

		try {
			historyService.findAttachmentHistory(123L);
		} catch (final ThrowableProblem e) {
			// assert that the expected exception is thrown
			assertEquals(Status.NOT_FOUND, e.getStatus());
			assertEquals("Attachment not found", e.getDetail());
		}

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindDecisionHistory() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findDecisionHistory(1L);
		assertFalse(jsonChanges.isEmpty());

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
	}

	@Test
	void testFindDecisionHistoryNothingFound() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(), PrettyValuePrinter.getDefault()));

		try {
			historyService.findDecisionHistory(123L);
		} catch (final ThrowableProblem e) {
			// assert that the expected exception is thrown
			assertEquals(Status.NOT_FOUND, e.getStatus());
			assertEquals("Decision not found", e.getDetail());
		}

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindErrandHistory() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findErrandHistory(1L);
		assertFalse(jsonChanges.isEmpty());

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
	}

	@Test
	void testFindErrandHistoryNothingFound() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(), PrettyValuePrinter.getDefault()));

		try {
			historyService.findErrandHistory(123L);
		} catch (final ThrowableProblem e) {
			// assert that the expected exception is thrown
			assertEquals(Status.NOT_FOUND, e.getStatus());
			assertEquals("Errand not found", e.getDetail());
		}

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindNoteHistory() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findNoteHistory(1L);
		assertFalse(jsonChanges.isEmpty());

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
	}

	@Test
	void testFindNoteHistoryNothingFound() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(), PrettyValuePrinter.getDefault()));

		try {
			historyService.findNoteHistory(1L);
		} catch (final ThrowableProblem e) {
			// assert that the expected exception is thrown
			assertEquals(Status.NOT_FOUND, e.getStatus());
			assertEquals("Note not found", e.getDetail());
		}

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}

	@Test
	void testFindHistoryStakholderFound() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(mock(Change.class)), PrettyValuePrinter.getDefault()));
		when(javers.getJsonConverter()).thenReturn(jsonConverter);
		when(jsonConverter.toJson(any(Changes.class))).thenReturn("[]");

		final var jsonChanges = historyService.findStakeholderHistory(1L);
		assertFalse(jsonChanges.isEmpty());

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
	}

	@Test
	void testFindHistoryNothingFound() {
		when(javers.findChanges(any(JqlQuery.class))).thenReturn(new Changes(List.of(), PrettyValuePrinter.getDefault()));

		try {
			historyService.findStakeholderHistory(1L);
		} catch (final ThrowableProblem e) {
			// assert that the expected exception is thrown
			assertEquals(Status.NOT_FOUND, e.getStatus());
			assertEquals("Stakeholder not found", e.getDetail());
		}

		verify(javers, times(1)).findChanges(any(JqlQuery.class));
		verifyNoMoreInteractions(javers);
	}
}
