package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.api.model.conversation.ConversationType.EXTERNAL;
import static se.sundsvall.casedata.api.model.conversation.ConversationType.INTERNAL;

import generated.se.sundsvall.messageexchange.KeyValues;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.ConversationType;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;
import se.sundsvall.casedata.service.scheduler.messageexchange.MessageExchangeScheduler;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {
	private static final String CONVERSATION_DEPARTMENT_NAME = "CONVERSATION";
	private static final String PARATRANSIT_DEPARTMENT_NAME = "PARATRANSIT";

	private static final String MESSAGE_EXCHANGE_NAMESPACE = "case-data";
	@Mock
	private MessageService messageServiceMock;
	@Mock
	private ErrandRepository errandRepositoryMock;
	@Mock
	private ConversationRepository conversationRepositoryMock;
	@Mock
	private MessageExchangeClient messageExchangeClientMock;
	@Mock
	private MessageExchangeScheduler messageExchangeSchedulerMock;
	@InjectMocks
	private ConversationService conversationService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(conversationService, "messageExchangeNamespace", MESSAGE_EXCHANGE_NAMESPACE);
	}

	@Test
	void createConversation() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversation = Conversation.builder().build();
		final var locationUri = "/some/location/123";
		final var conversationId = "123";

		when(messageExchangeClientMock.createConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), any()))
			.thenReturn(ResponseEntity.created(URI.create(locationUri)).build());

		when(conversationRepositoryMock.save(any(ConversationEntity.class)))
			.thenReturn(ConversationEntity.builder().withId(conversationId).build());

		// Act
		final var result = conversationService.createConversation(municipalityId, namespace, errandId, conversation);

		// Assert
		assertThat(result).isNotNull().isEqualTo(conversationId);
		verify(messageExchangeClientMock).createConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), any());
		verify(conversationRepositoryMock).save(any(ConversationEntity.class));
		verifyNoMoreInteractions(messageExchangeClientMock, conversationRepositoryMock);
		verifyNoInteractions(messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void createConversationWithMessageExchangeError() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversation = Conversation.builder().build();

		when(messageExchangeClientMock.createConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), any()))
			.thenReturn(ResponseEntity.internalServerError().build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.createConversation(municipalityId, namespace, errandId, conversation)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to create conversation in Message Exchange");

		verify(messageExchangeClientMock).createConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), any());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(conversationRepositoryMock, messageExchangeSchedulerMock, messageServiceMock);

	}

	@Test
	void createConversationWithMissingLocation() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversation = Conversation.builder().build();

		when(messageExchangeClientMock.createConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), any()))
			.thenReturn(ResponseEntity.created(null).build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.createConversation(municipalityId, namespace, errandId, conversation)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to create conversation in Message Exchange");

		verify(messageExchangeClientMock).createConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), any());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(conversationRepositoryMock, messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void getConversation() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var topic = "topic";
		final var type = "INTERNAL";
		final var relationIds = List.of("relationId");

		final var conversationEntity = ConversationEntity.builder()
			.withId(conversationId)
			.withMessageExchangeId(messageExchangeId)
			.withRelationIds(relationIds)
			.withType(type)
			.build();

		final var conversationResponse = new generated.se.sundsvall.messageexchange.Conversation()
			.topic(topic)
			.externalReferences(List.of(new KeyValues().key("relationIds").values(relationIds)));

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(conversationEntity));

		when(messageExchangeClientMock.getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId))
			.thenReturn(ResponseEntity.ok(conversationResponse));

		// Act
		final var result = conversationService.getConversation(municipalityId, namespace, errandId, conversationId);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(conversationId);
		assertThat(result.getTopic()).isEqualTo(topic);
		assertThat(result.getRelationIds()).isEqualTo(relationIds);
		assertThat(result.getType()).isEqualTo(ConversationType.valueOf(type));

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId);
		verify(messageExchangeSchedulerMock).triggerSyncConversationsAsync();
		verifyNoMoreInteractions(messageExchangeClientMock, conversationRepositoryMock);
		verifyNoInteractions(messageServiceMock);
	}

	@Test
	void getConversationWithMessageExchangeError() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId))
			.thenReturn(ResponseEntity.internalServerError().build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversation(municipalityId, namespace, errandId, conversationId)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to retrieve conversation from Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId);
		verifyNoMoreInteractions(messageExchangeClientMock, conversationRepositoryMock);
		verifyNoInteractions(messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void getConversationNotFoundLocally() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.empty());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversation(municipalityId, namespace, errandId, conversationId)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Not Found: Conversation not found in local database");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verifyNoInteractions(messageExchangeClientMock);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void getConversationNotFoundInMessageExchange() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId))
			.thenReturn(ResponseEntity.notFound().build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversation(municipalityId, namespace, errandId, conversationId)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to retrieve conversation from Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId);
		verifyNoMoreInteractions(messageExchangeClientMock, conversationRepositoryMock);
		verifyNoInteractions(messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void getConversationNullFromMessageExchange() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId))
			.thenReturn(ResponseEntity.ok(null));

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversation(municipalityId, namespace, errandId, conversationId)).isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Not Found: Conversation not found in Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getConversation(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId);
		verifyNoMoreInteractions(messageExchangeClientMock, conversationRepositoryMock);
		verifyNoInteractions(messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void getConversations() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var topic = "topic";
		final var type = "INTERNAL";
		final var relationIds = List.of("relationId");

		final var conversationEntity = ConversationEntity.builder()
			.withId(conversationId)
			.withMessageExchangeId(messageExchangeId)
			.withRelationIds(relationIds)
			.withType(type)
			.withTopic(topic)
			.build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandId(municipalityId, namespace, String.valueOf(errandId)))
			.thenReturn(List.of(conversationEntity));

		// Act
		final var result = conversationService.getConversations(municipalityId, namespace, errandId);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).isNotNull().hasNoNullFieldsOrPropertiesExcept("participants", "metadata");
		assertThat(result.getFirst().getId()).isEqualTo(conversationId);
		assertThat(result.getFirst().getRelationIds()).isEqualTo(conversationEntity.getRelationIds());
		assertThat(result.getFirst().getTopic()).isEqualTo(topic);

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandId(municipalityId, namespace, String.valueOf(errandId));
		verifyNoInteractions(messageExchangeClientMock, messageExchangeSchedulerMock);
		verifyNoMoreInteractions(conversationRepositoryMock, messageServiceMock);

	}

	@Test
	void updateConversation() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var topic = "updatedTopic";
		final var type = "INTERNAL";
		final var relationIds = List.of("relationId");

		final var conversationEntity = ConversationEntity.builder()
			.withId(conversationId)
			.withMessageExchangeId(messageExchangeId)
			.withRelationIds(relationIds)
			.withType(type)
			.build();

		final var conversationRequest = Conversation.builder()
			.withTopic(topic)
			.withType(ConversationType.valueOf(type))
			.withRelationIds(relationIds)
			.build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(conversationEntity));

		when(messageExchangeClientMock.updateConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any()))
			.thenReturn(ResponseEntity.ok(new generated.se.sundsvall.messageexchange.Conversation().topic(topic)));

		when(conversationRepositoryMock.save(any(ConversationEntity.class)))
			.thenReturn(conversationEntity);

		// Act
		final var result = conversationService.updateConversation(municipalityId, namespace, errandId, conversationId, conversationRequest);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept();
		assertThat(result.getTopic()).isEqualTo(topic);
		assertThat(result.getRelationIds()).isEqualTo(relationIds);

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).updateConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any());
		verify(conversationRepositoryMock).save(any(ConversationEntity.class));
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
		verifyNoInteractions(messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void updateConversationMessageExchangeError() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var request = Conversation.builder().withTopic("updatedTopic").build();
		final var entity = ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(entity));
		when(messageExchangeClientMock.updateConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any()))
			.thenReturn(ResponseEntity.internalServerError().build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.updateConversation(municipalityId, namespace, errandId, conversationId, request))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to update conversation in Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).updateConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any());
		verifyNoMoreInteractions(conversationRepositoryMock);
		verifyNoInteractions(messageExchangeSchedulerMock);
	}

	@Test
	void updateConversationNullResponseBody() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var request = Conversation.builder().withTopic("updatedTopic").build();
		final var entity = ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(entity));
		when(messageExchangeClientMock.updateConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any()))
			.thenReturn(ResponseEntity.ok(null));

		// Act & Assert
		assertThatThrownBy(() -> conversationService.updateConversation(municipalityId, namespace, errandId, conversationId, request))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to update conversation in Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).updateConversation(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any());
		verifyNoMoreInteractions(conversationRepositoryMock);
		verifyNoInteractions(messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void createExternalMessage() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var messageContent = Message.builder().build();

		final var attachment = new MockMultipartFile("attachments", "attachment.txt".getBytes());

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withType(EXTERNAL.name()).withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any()))
			.thenReturn(ResponseEntity.ok().build());

		// Act
		conversationService.createMessage(municipalityId, namespace, errandId, conversationId, messageContent, List.of(attachment));

		// Assert
		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any());
		verify(messageExchangeSchedulerMock).triggerSyncConversationsAsync();
		verify(messageServiceMock).sendMessageNotification(municipalityId, namespace, errandId, CONVERSATION_DEPARTMENT_NAME);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock, messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void createInternalMessageForParatransitErrand() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var messageContent = Message.builder().build();

		final var attachment = new MockMultipartFile("attachments", "attachment.txt".getBytes());

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withType(INTERNAL.name()).withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any()))
			.thenReturn(ResponseEntity.ok().build());

		when(errandRepositoryMock.getReferenceById(errandId)).thenReturn(ErrandEntity.builder().withCaseType("PaRaTrAnSiT_SOMETHING_SOMETHING").build());

		// Act
		conversationService.createMessage(municipalityId, namespace, errandId, conversationId, messageContent, List.of(attachment));

		// Assert
		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any());
		verify(messageExchangeSchedulerMock).triggerSyncConversationsAsync();
		verify(messageServiceMock).sendEmailNotification(municipalityId, namespace, errandId, PARATRANSIT_DEPARTMENT_NAME);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock, messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void createInternalMessageForNonParatransitErrand() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var messageContent = Message.builder().build();

		final var attachment = new MockMultipartFile("attachments", "attachment.txt".getBytes());

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withType(INTERNAL.name()).withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any()))
			.thenReturn(ResponseEntity.ok().build());

		// Act
		conversationService.createMessage(municipalityId, namespace, errandId, conversationId, messageContent, List.of(attachment));

		// Assert
		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any());
		verify(messageExchangeSchedulerMock).triggerSyncConversationsAsync();
		verify(messageServiceMock).sendEmailNotification(municipalityId, namespace, errandId, CONVERSATION_DEPARTMENT_NAME);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock, messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void createMessageSendIntegrationThrowsException() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var messageContent = Message.builder().build();

		final var attachment = new MockMultipartFile("attachments", "attachment.txt".getBytes());

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withType(INTERNAL.name()).withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any()))
			.thenReturn(ResponseEntity.ok().build());

		doThrow(new RuntimeException("Test")).when(messageServiceMock).sendEmailNotification(any(), any(), any(), any());

		// Act
		conversationService.createMessage(municipalityId, namespace, errandId, conversationId, messageContent, List.of(attachment));

		// Assert
		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any());
		verify(messageExchangeSchedulerMock).triggerSyncConversationsAsync();
		verify(messageServiceMock).sendEmailNotification(municipalityId, namespace, errandId, CONVERSATION_DEPARTMENT_NAME);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock, messageExchangeSchedulerMock, messageServiceMock);
	}

	@Test
	void createMessageNon2xxResponse() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";
		final var messageRequest = Message.builder().build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any()))
			.thenReturn(ResponseEntity.internalServerError().build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.createMessage(municipalityId, namespace, errandId, conversationId, messageRequest, null))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to create message in Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any());
		verifyNoInteractions(messageExchangeSchedulerMock, messageServiceMock);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
	}

	@Test
	void getMessages() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";
		final var pageable = PageRequest.of(0, 10);
		final var conversationEntity = ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(conversationEntity));

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any()))
			.thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(new generated.se.sundsvall.messageexchange.Message().type(generated.se.sundsvall.messageexchange.Message.TypeEnum.USER_CREATED)))));

		// Act
		final var result = conversationService.getMessages(municipalityId, namespace, errandId, conversationId, pageable);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeSchedulerMock).triggerSyncConversationsAsync();
		verify(messageExchangeClientMock).getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, null, pageable);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock, messageExchangeSchedulerMock);
		verifyNoInteractions(messageServiceMock);
	}

	@Test
	void getMessagesNon2xxResponse() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";
		final var pageable = PageRequest.of(0, 10);

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, null, pageable))
			.thenReturn(ResponseEntity.internalServerError().build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getMessages(municipalityId, namespace, errandId, conversationId, pageable))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to retrieve messages from Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, null, pageable);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
		verifyNoInteractions(messageServiceMock, messageExchangeSchedulerMock);
	}

	@Test
	void getMessagesNullResponseBody() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";
		final var pageable = PageRequest.of(0, 10);

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, null, pageable))
			.thenReturn(ResponseEntity.ok(null));

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getMessages(municipalityId, namespace, errandId, conversationId, pageable))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to retrieve messages from Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, null, pageable);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
		verifyNoInteractions(messageServiceMock, messageExchangeSchedulerMock);
	}

	@Test
	void getConversationMessageAttachment() throws IOException {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "convId";
		final var messageId = "msgId";
		final var attachmentId = "attId";
		final var messageExchangeId = "exchangeId";
		final var filename = "file.txt";
		final var contentType = org.springframework.http.MediaType.TEXT_PLAIN;
		final var content = "test content".getBytes();

		final var conversationEntity = ConversationEntity.builder()
			.withMessageExchangeId(messageExchangeId)
			.build();

		final var inputStream = new java.io.ByteArrayInputStream(content);
		final var inputStreamResource = new org.springframework.core.io.InputStreamResource(inputStream) {
			@Override
			public String getFilename() {
				return filename;
			}
		};

		when(messageExchangeClientMock.readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId))
			.thenReturn(ResponseEntity.ok()
				.contentType(contentType)
				.contentLength(content.length)
				.body(inputStreamResource));

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(conversationEntity));

		final var response = new org.springframework.mock.web.MockHttpServletResponse();

		// Act
		conversationService.getConversationMessageAttachment(municipalityId, namespace, errandId, conversationId, messageId, attachmentId, response);

		// Assert
		assertThat(response.getStatus()).isEqualTo(200);
		assertThat(response.getContentType()).isEqualTo(contentType.toString());
		assertThat(response.getHeader("Content-Disposition")).contains(filename);
		assertThat(response.getContentAsByteArray()).isEqualTo(content);

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId);
	}

	@Test
	void getConversationMessageAttachmentMissingExchangeId() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "convId";
		final var messageId = "msgId";
		final var attachmentId = "attId";

		final var conversationEntity = ConversationEntity.builder().withMessageExchangeId(null).build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(conversationEntity));

		final var response = new org.springframework.mock.web.MockHttpServletResponse();

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversationMessageAttachment(municipalityId, namespace, errandId, conversationId, messageId, attachmentId, response))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Conversation not found in local database");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verifyNoInteractions(messageExchangeClientMock);
	}

	@Test
	void getConversationMessageAttachmentNon2xxResponse() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "convId";
		final var messageId = "msgId";
		final var attachmentId = "attId";
		final var messageExchangeId = "exchangeId";

		final var conversationEntity = ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(conversationEntity));
		when(messageExchangeClientMock.readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId))
			.thenReturn(ResponseEntity.status(404).build());

		final var response = new org.springframework.mock.web.MockHttpServletResponse();

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversationMessageAttachment(municipalityId, namespace, errandId, conversationId, messageId, attachmentId, response))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Attachment not found or invalid in Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId);
	}

	@Test
	void getConversationMessageAttachmentNullBody() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "convId";
		final var messageId = "msgId";
		final var attachmentId = "attId";
		final var messageExchangeId = "exchangeId";

		final var conversationEntity = ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(conversationEntity));
		when(messageExchangeClientMock.readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId))
			.thenReturn(ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(null));

		final var response = new org.springframework.mock.web.MockHttpServletResponse();

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversationMessageAttachment(municipalityId, namespace, errandId, conversationId, messageId, attachmentId, response))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Not Found: Attachment not found or invalid in Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId);
	}

	@Test
	void getConversationMessageAttachmentNullContentType() {
		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "convId";
		final var messageId = "msgId";
		final var attachmentId = "attId";
		final var messageExchangeId = "exchangeId";
		final var filename = "file.txt";
		final var content = "test content".getBytes();
		final var inputStream = new java.io.ByteArrayInputStream(content);
		final var inputStreamResource = new org.springframework.core.io.InputStreamResource(inputStream) {
			@Override
			public String getFilename() {
				return filename;
			}
		};

		final var conversationEntity = ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build();

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.of(conversationEntity));
		when(messageExchangeClientMock.readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId))
			.thenReturn(ResponseEntity.ok().body(inputStreamResource));

		final var response = new org.springframework.mock.web.MockHttpServletResponse();

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getConversationMessageAttachment(municipalityId, namespace, errandId, conversationId, messageId, attachmentId, response))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Not Found: Attachment not found or invalid in Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).readErrandAttachment(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, messageId, attachmentId);
	}
}
