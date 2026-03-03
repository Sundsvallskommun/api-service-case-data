package se.sundsvall.casedata.service.scheduler.webmessagecollector;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WebMessageCollectorSchedulerTest {

	@Mock
	private WebMessageCollectorWorker webMessageCollectorWorkerMock;

	@InjectMocks
	private WebMessageCollectorScheduler webMessageCollectorScheduler;

	@Test
	void getAndProcessMessages() {

		// Act
		webMessageCollectorScheduler.getAndProcessMessages();

		// Verify
		verify(webMessageCollectorWorkerMock).getAndProcessMessages();
		verify(webMessageCollectorWorkerMock).deleteMessages(Mockito.<Map<String, List<Integer>>>any());
		verifyNoMoreInteractions(webMessageCollectorWorkerMock);
	}

}
