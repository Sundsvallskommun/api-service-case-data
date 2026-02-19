package se.sundsvall.casedata.integration.eventlog;

import generated.se.sundsvall.eventlog.Event;
import generated.se.sundsvall.eventlog.EventType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createStatus;

@ExtendWith(MockitoExtension.class)
class EventlogIntegrationTest {

	@Mock
	private EventlogClient eventlogClientMock;

	@InjectMocks
	private EventlogIntegration eventlogIntegration;

	@Captor
	private ArgumentCaptor<Event> eventCaptor;

	@Test
	void sendEventlogEvent() {

		// Arrange
		final var errand = createErrandEntity();
		final var status = createStatus();

		// Act
		eventlogIntegration.sendEventlogEvent(MUNICIPALITY_ID, errand, status);

		// Assert
		verify(eventlogClientMock).createEvent(eq(MUNICIPALITY_ID), eq(String.valueOf(errand.getId())), eventCaptor.capture());

		final var capturedEvent = eventCaptor.getValue();
		assertThat(capturedEvent.getType()).isEqualTo(EventType.UPDATE);
		assertThat(capturedEvent.getOwner()).isEqualTo("CaseData");
		assertThat(capturedEvent.getMessage()).isEqualTo("Status updated to " + status.getStatusType());
		assertThat(capturedEvent.getSourceType()).isEqualTo("Errand");
		assertThat(capturedEvent.getMetadata()).hasSize(1);
		assertThat(capturedEvent.getMetadata().getFirst().getKey()).isEqualTo("Status");
		assertThat(capturedEvent.getMetadata().getFirst().getValue()).isEqualTo(status.getStatusType());

		verifyNoMoreInteractions(eventlogClientMock);
	}

	@Test
	void sendEventlogEventFailureDoesNotPropagate() {

		// Arrange
		final var errand = createErrandEntity();
		final var status = createStatus();
		doThrow(new RuntimeException("Eventlog unavailable")).when(eventlogClientMock).createEvent(any(), any(), any(Event.class));

		// Act - should not throw
		eventlogIntegration.sendEventlogEvent(MUNICIPALITY_ID, errand, status);

		// Assert
		verify(eventlogClientMock).createEvent(eq(MUNICIPALITY_ID), eq(String.valueOf(errand.getId())), any(Event.class));
		verifyNoMoreInteractions(eventlogClientMock);
	}
}
