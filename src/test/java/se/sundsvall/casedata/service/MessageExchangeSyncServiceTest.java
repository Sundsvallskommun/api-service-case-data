package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.ADMINISTRATOR;

import generated.se.sundsvall.messageexchange.Conversation;
import generated.se.sundsvall.messageexchange.Identifier;
import generated.se.sundsvall.messageexchange.KeyValues;
import generated.se.sundsvall.messageexchange.Message;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.integration.db.ConversationRepository;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
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
	private NotificationService notificationServiceMock;
	@Mock
	private ErrandRepository errandRepositoryMock;
	@InjectMocks
	private MessageExchangeSyncService service;

	@Test
	void syncConversation() {

		// Arrange
		final var id = "id";
		final var messageExchangeId = "messageExchangeId";
		final var errandId = "1";
		final var namespace = "namespace";
		final var municipalityId = "municipalityId";
		final var topic = "topic";
		final var type = "INTERNAL";
		final var relationIds = List.of("relationId");
		final var latestSyncedSequenceNumber = 123L;
		final var targetRelationId = "targetRelationId";
		final var newLatestSyncedSequenceNumber = 456L;
		final var adAccount = "adAccount";

		final var errandEntity = ErrandEntity.builder()
			.withId(Long.parseLong(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withStakeholders(List.of(StakeholderEntity.builder().withAdAccount(adAccount).withRoles(List.of(ADMINISTRATOR.name())).build()))
			.build();

		final var conversationEntity = ConversationEntity.builder()
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
			.participants(List.of(new Identifier().type("identifier").value("identifierValue")))
			.externalReferences(List.of(new KeyValues().key("relationId").values(List.of(relationIds.getFirst()))))
			.metadata(List.of(new KeyValues().key("metadata").values(List.of("metadataValue"))))
			.topic(topic)
			.latestSequenceNumber(newLatestSyncedSequenceNumber);

		when(errandRepositoryMock.getReferenceById(any())).thenReturn(errandEntity);
		when(messageExchangeClientMock.getMessages(any(), any(), any(), any(), any())).thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(new generated.se.sundsvall.messageexchange.Message()))));
		when(conversationRepositoryMock.save(conversationEntity)).thenReturn(conversationEntity);

		// Act
		service.syncConversation(conversationEntity, conversation);

		// Assert
		verify(errandRepositoryMock).getReferenceById(1L);
		verify(messageExchangeClientMock).getMessages(municipalityId, namespace, messageExchangeId, "sequenceNumber.id >123", Pageable.unpaged());
		verify(notificationServiceMock).create(eq(municipalityId), eq(namespace), any(), same(errandEntity));
		verify(conversationRepositoryMock).save(conversationEntity);

		verifyNoMoreInteractions(conversationRepositoryMock, messageExchangeClientMock);
		verifyNoInteractions(attachmentServiceMock);
	}

	@Test
	void syncMessagesAllMatchAdministratorOwner() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var messageExchangeId = "messageExchangeId";
		final var errandAdministratorOwnerId = "errandAdministratorOwnerId";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMessageExchangeId(messageExchangeId)
			.withLatestSyncedSequenceNumber(123L)
			.build();

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(namespace), any(), any(), any()))
			.thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(new generated.se.sundsvall.messageexchange.Message().createdBy(new Identifier(errandAdministratorOwnerId))))));

		// Act
		var result = service.syncMessages(conversationEntity, errandAdministratorOwnerId);

		// Assert
		assertThat(result).isTrue();
		verify(messageExchangeClientMock).getMessages(municipalityId, namespace, messageExchangeId, "sequenceNumber.id >123", Pageable.unpaged());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(attachmentServiceMock, conversationRepositoryMock);
	}

	@Test
	void syncMessagesNotAllMatchAdministratorOwner() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var messageExchangeId = "messageExchangeId";
		final var errandAdministratorOwnerId = "errandAdministratorOwnerId";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMessageExchangeId(messageExchangeId)
			.withLatestSyncedSequenceNumber(123L)
			.build();

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(namespace), any(), any(), any()))
			.thenReturn(ResponseEntity.ok(new PageImpl<>(List.of(
				new generated.se.sundsvall.messageexchange.Message().createdBy(new Identifier(errandAdministratorOwnerId)),
				new generated.se.sundsvall.messageexchange.Message().createdBy(new Identifier("otherUserId"))))));

		// Act
		var result = service.syncMessages(conversationEntity, errandAdministratorOwnerId);

		// Assert
		assertThat(result).isFalse();
		verify(messageExchangeClientMock).getMessages(municipalityId, namespace, messageExchangeId, "sequenceNumber.id >123", Pageable.unpaged());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(attachmentServiceMock, conversationRepositoryMock);
	}

	@Test
	void syncMessagesNoMessages() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var messageExchangeId = "messageExchangeId";
		final var errandAdministratorOwnerId = "errandAdministratorOwnerId";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMessageExchangeId(messageExchangeId)
			.withLatestSyncedSequenceNumber(123L)
			.build();

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(namespace), any(), any(), any()))
			.thenReturn(ResponseEntity.ok(new PageImpl<>(List.of())));

		// Act
		var result = service.syncMessages(conversationEntity, errandAdministratorOwnerId);

		// Assert
		assertThat(result).isTrue();
		verify(messageExchangeClientMock).getMessages(municipalityId, namespace, messageExchangeId, "sequenceNumber.id >123", Pageable.unpaged());
		verifyNoMoreInteractions(messageExchangeClientMock);
		verifyNoInteractions(attachmentServiceMock, conversationRepositoryMock);
	}

	@Test
	void syncMessagesNoResponse() {
		// Arrange
		final var errandId = 123L;
		final var municipalityId = "municipalityId";
		final var namespace = "namespace";
		final var messageExchangeId = "messageExchangeId";
		final var errandAdministratorOwnerId = "errandAdministratorOwnerId";
		final var conversationEntity = ConversationEntity.builder()
			.withErrandId(String.valueOf(errandId))
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withMessageExchangeId(messageExchangeId)
			.withLatestSyncedSequenceNumber(123L)
			.build();

		when(messageExchangeClientMock.getMessages(eq(municipalityId), eq(namespace), any(), any(), any()))
			.thenReturn(null);

		// Act & Assert
		assertThatThrownBy(() -> service.syncMessages(conversationEntity, errandAdministratorOwnerId))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Failed to retrieve messages from Message Exchange");

		verify(messageExchangeClientMock).getMessages(municipalityId, namespace, messageExchangeId, "sequenceNumber.id >123", Pageable.unpaged());
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
