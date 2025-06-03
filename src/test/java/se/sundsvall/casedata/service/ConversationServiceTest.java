package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import se.sundsvall.casedata.api.model.Attachment;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.ConversationType;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;

@ExtendWith(MockitoExtension.class)
class ConversationServiceTest {

	private static final String MESSAGE_EXCHANGE_NAMESPACE = "case-data";
	@Mock
	private ConversationRepository conversationRepositoryMock;
	@Mock
	private MessageExchangeClient messageExchangeClientMock;
	@Mock
	private AttachmentService attachmentServiceMock;
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
		verifyNoInteractions(attachmentServiceMock);
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
		verifyNoInteractions(conversationRepositoryMock, attachmentServiceMock);

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
		verifyNoInteractions(conversationRepositoryMock, attachmentServiceMock);
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
			.topic(topic);

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
		verify(conversationRepositoryMock).save(conversationEntity);
		verifyNoMoreInteractions(messageExchangeClientMock, conversationRepositoryMock);
		verifyNoInteractions(attachmentServiceMock);
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
		verifyNoInteractions(attachmentServiceMock);
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
		verifyNoMoreInteractions(conversationRepositoryMock, attachmentServiceMock);
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
		verifyNoInteractions(attachmentServiceMock);
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
		verifyNoInteractions(attachmentServiceMock);
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
		verifyNoInteractions(messageExchangeClientMock, attachmentServiceMock);
		verifyNoMoreInteractions(conversationRepositoryMock);

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
		verifyNoInteractions(attachmentServiceMock);
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
		verifyNoInteractions(attachmentServiceMock);
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
		verifyNoInteractions(attachmentServiceMock);
	}

	@Test
	void createMessage() {

		// Arrange
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var errandId = 123L;
		final var conversationId = "123";
		final var messageExchangeId = "messageExchangeId";

		final var messageContent = Message.builder().build();

		final var attachment = new MockMultipartFile("attachments", "attachment.txt".getBytes());

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any()))
			.thenReturn(ResponseEntity.ok().build());

		// Act
		conversationService.createMessage(municipalityId, namespace, errandId, conversationId, messageContent, List.of(attachment));

		// Assert
		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).createMessage(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any(), any());
		verify(attachmentServiceMock).create(eq(errandId), any(Attachment.class), eq(municipalityId), eq(namespace));
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock, attachmentServiceMock);
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
		verifyNoInteractions(attachmentServiceMock);
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

		when(conversationRepositoryMock.findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId))
			.thenReturn(Optional.ofNullable(ConversationEntity.builder().withMessageExchangeId(messageExchangeId).build()));

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(MESSAGE_EXCHANGE_NAMESPACE), eq(messageExchangeId), any()))
			.thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(new generated.se.sundsvall.messageexchange.Message()))));

		// Act
		final var result = conversationService.getMessages(municipalityId, namespace, errandId, conversationId, pageable);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, pageable);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
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

		when(messageExchangeClientMock.getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, pageable))
			.thenReturn(ResponseEntity.internalServerError().build());

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getMessages(municipalityId, namespace, errandId, conversationId, pageable))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to retrieve messages from Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, pageable);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
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

		when(messageExchangeClientMock.getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, pageable))
			.thenReturn(ResponseEntity.ok(null));

		// Act & Assert
		assertThatThrownBy(() -> conversationService.getMessages(municipalityId, namespace, errandId, conversationId, pageable))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("Failed to retrieve messages from Message Exchange");

		verify(conversationRepositoryMock).findByMunicipalityIdAndNamespaceAndErrandIdAndId(municipalityId, namespace, String.valueOf(errandId), conversationId);
		verify(messageExchangeClientMock).getMessages(municipalityId, MESSAGE_EXCHANGE_NAMESPACE, messageExchangeId, pageable);
		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
	}
}
