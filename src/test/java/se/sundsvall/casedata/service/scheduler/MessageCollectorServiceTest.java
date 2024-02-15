package se.sundsvall.casedata.service.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.MessageRepository;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.Message;
import se.sundsvall.casedata.integration.webmessagecollector.WebMessageCollectorClient;
import se.sundsvall.casedata.integration.webmessagecollector.configuration.WebMessageCollectorProperties;

import generated.se.sundsvall.webmessagecollector.MessageDTO;

@ExtendWith(MockitoExtension.class)
class MessageCollectorServiceTest {

	@Mock
	private WebMessageCollectorProperties webMessageCollectorProperties;

	@Mock
	private MessageRepository messageRepositoryMock;

	@Mock
	private WebMessageCollectorClient webMessageCollectorClientMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private MessageMapper messageMapperMock;

	@InjectMocks
	private MessageCollectorService messageCollectorService;

	@Test
	void getAndProcessMessages() {
		when(webMessageCollectorClientMock.getMessages(any(String.class))).thenReturn(createMessages());
		when(messageRepositoryMock.save(any())).thenReturn(Message.builder().build());
		when(errandRepositoryMock.findByExternalCaseId(any())).thenReturn(Errand.builder().withExternalCaseId("someExternalCaseId").build());
		when(webMessageCollectorProperties.familyIds()).thenReturn(List.of("123"));

		messageCollectorService.getAndProcessMessages();

		verify(webMessageCollectorClientMock).getMessages(any(String.class));
		verify(webMessageCollectorClientMock).deleteMessages(any());
		verify(messageRepositoryMock).save(any());
		verify(messageMapperMock).toMessageEntity(any(), any());
	}

	@Test
	void getAndProcessMessagesWhenNonMatchingErrandExists() {
		when(webMessageCollectorClientMock.getMessages(any(String.class))).thenReturn(createMessages());
		when(webMessageCollectorProperties.familyIds()).thenReturn(List.of("123"));

		messageCollectorService.getAndProcessMessages();

		verify(webMessageCollectorClientMock).getMessages(any(String.class));
		verify(webMessageCollectorClientMock).deleteMessages(any());
		verify(messageRepositoryMock, never()).save(any());
		verify(messageMapperMock, never()).toMessageEntity(any(), any());
	}

	private List<MessageDTO> createMessages() {
		return List.of(new MessageDTO()
			.direction(MessageDTO.DirectionEnum.INBOUND)
			.familyId("1")
			.externalCaseId("1")
			.message("message")
			.messageId("1")
			.sent("2021-01-01T00:00:00.000Z")
			.username("username")
			.firstName("firstName")
			.lastName("lastName")
			.email("email")
			.userId("1"));
	}

}
