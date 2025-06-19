package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.api.model.conversation.ConversationType.INTERNAL;

import generated.se.sundsvall.messageexchange.Conversation;
import generated.se.sundsvall.messageexchange.Identifier;
import generated.se.sundsvall.messageexchange.KeyValues;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.messageexchange.MessageExchangeClient;

@ExtendWith(MockitoExtension.class)
class MessageExchangeSyncServiceTest {

	@Mock
	private MessageExchangeClient messageExchangeClientMock;
	@Mock
	private AttachmentService attachmentServiceMock;
	@Mock
	private ConversationRepository conversationRepositoryMock;
	@Mock
	private ApplicationEventPublisher applicationEventPublisherMock;
	@InjectMocks
	private MessageExchangeSyncService service;

	@Test
	void syncConversation() {

		// Arrange
		final var id = "id";
		final var messageExchangeId = "messageExchangeId";
		final var errandId = "errandId";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var topic = "topic";
		final var type = "INTERNAL";
		final var relationIds = List.of("relationId");
		final var latestSyncedSequenceNumber = 123L;
		final var targetRelationId = "targetRelationId";
		final var newLatestSyncedSequenceNumber = 456L;
		final var filter = "sequenceNumber >" + latestSyncedSequenceNumber;

		final var entity = ConversationEntity.builder()
			.withId(id)
			.withMessageExchangeId(messageExchangeId)
			.withErrandId(errandId)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.withLatestSyncedSequenceNumber(latestSyncedSequenceNumber)
			.withTargetRelationId(targetRelationId)
			.build();

		final var conversation = new Conversation()
			.id(id)
			.namespace(namespace)
			.municipalityId(municipalityId)
			.participants(List.of(new Identifier().type("relation").value(relationIds.getFirst())))
			.externalReferences(List.of(new KeyValues().key("relationId").values(List.of(relationIds.getFirst()))))
			.metadata(List.of(new KeyValues().key("latestSyncedSequenceNumber").values(List.of(String.valueOf(newLatestSyncedSequenceNumber)))))
			.topic(topic)
			.latestSequenceNumber(newLatestSyncedSequenceNumber);

		when(conversationRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		final var result = service.syncConversation(entity, conversation);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getTopic()).isEqualTo(topic);
		assertThat(result.getType()).isEqualTo(INTERNAL);
		assertThat(result.getRelationIds()).isEqualTo(relationIds);
		assertThat(result.getParticipants()).hasSize(1);
		assertThat(result.getMetadata()).hasSize(1);

		verify(conversationRepositoryMock).save(entity);
		verify(applicationEventPublisherMock).publishEvent(entity);

		verifyNoMoreInteractions(conversationRepositoryMock, applicationEventPublisherMock);
		verifyNoInteractions(messageExchangeClientMock, attachmentServiceMock);
	}

	@Test
	void syncMessages() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(namespace), any(), any(), any()))
			.thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(new generated.se.sundsvall.messageexchange.Message()))));

		// Act
		service.syncMessages(conversationEntity);

		// Assert
		verify(messageExchangeClientMock).getMessages(eq(municipalityId), eq(namespace), any(), any(), any());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(attachmentServiceMock, conversationRepositoryMock);
	}

	@Test
	void syncMessagesNoMessages() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(namespace), any(), any(), any()))
			.thenReturn(ResponseEntity.ok(new PageImpl<>(List.of())));

		// Act
		service.syncMessages(conversationEntity);

		// Assert
		verify(messageExchangeClientMock).getMessages(eq(municipalityId), eq(namespace), any(), any(), any());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(attachmentServiceMock, conversationRepositoryMock);
	}

	@Test
	void syncMessagesNoResponse() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(namespace), any(), any(), any()))
			.thenReturn(null);

		// Act & Assert
		assertThatThrownBy(() -> service.syncMessages(conversationEntity))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Failed to retrieve messages from Message Exchange");

		verify(messageExchangeClientMock).getMessages(eq(municipalityId), eq(namespace), any(), any(), any());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(attachmentServiceMock, conversationRepositoryMock);
	}

	@Test
	void syncAttachment() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.build();
		final var message = new generated.se.sundsvall.messageexchange.Message();
		final var attachment = new generated.se.sundsvall.messageexchange.Attachment().id("attachmentId");

		when(messageExchangeClientMock.readErrandAttachment(eq(municipalityId), any(), any(), any(), eq(attachment.getId())))
			.thenReturn(ResponseEntity.ok()
				.header("Content-Type", "application/octet-stream")
				.body(new InputStreamResource(new ByteArrayInputStream(new byte[0]))));

		// Act
		service.syncAttachment(conversationEntity, message, attachment);

		// Assert
		verify(messageExchangeClientMock).readErrandAttachment(eq(municipalityId), any(), any(), any(), eq(attachment.getId()));
		verify(attachmentServiceMock).create(eq(errandId), any(), eq(municipalityId), eq(namespace));
		verifyNoMoreInteractions(attachmentServiceMock, messageExchangeClientMock);
		verifyNoInteractions(conversationRepositoryMock);
	}

	@Test
	void saveAttachment() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var file = ResponseEntity.ok()
			.header("Content-Type", "application/octet-stream")
			.body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));

		// Act
		service.saveAttachment(errandId, municipalityId, namespace, file);

		// Assert
		verify(attachmentServiceMock).create(eq(errandId), any(), eq(municipalityId), eq(namespace));
		verifyNoMoreInteractions(attachmentServiceMock);
		verifyNoInteractions(conversationRepositoryMock, messageExchangeClientMock);
	}

	@Test
	void saveAttachmentNullFile() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final ResponseEntity<InputStreamResource> file = ResponseEntity.ok()
			.header("Content-Type", "application/octet-stream")
			.build();

		// Act & Assert
		assertThatThrownBy(() -> service.saveAttachment(errandId, municipalityId, namespace, file))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Failed to retrieve attachment from Message Exchange");

		verifyNoInteractions(attachmentServiceMock, conversationRepositoryMock, messageExchangeClientMock);
	}

	@Test
	void saveAttachmentNoContentType() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final ResponseEntity<InputStreamResource> file = ResponseEntity.ok()
			.body(new InputStreamResource(new ByteArrayInputStream(new byte[0])));

		// Act & Assert
		assertThatThrownBy(() -> service.saveAttachment(errandId, municipalityId, namespace, file))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Failed to retrieve attachment from Message Exchange");

		verifyNoInteractions(conversationRepositoryMock, messageExchangeClientMock);
	}

}
