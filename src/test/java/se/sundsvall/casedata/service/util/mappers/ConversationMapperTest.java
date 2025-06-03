package se.sundsvall.casedata.service.util.mappers;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.ConversationType;
import se.sundsvall.casedata.api.model.conversation.Identifier;
import se.sundsvall.casedata.api.model.conversation.KeyValues;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;

class ConversationMapperTest {

	@Test
	void toConversationEntity() {
		// Arrange
		final var topic = "Test Topic";
		final var type = ConversationType.INTERNAL;
		final var relationIds = List.of("relation1", "relation2");
		final var request = Conversation.builder()
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.build();
		final var municipalityId = "2281";
		final var namespace = "namespace";
		final var errandId = 12345L;
		final var messageExchangeId = "message-exchange-id-123";

		// Act
		final var conversation = ConversationMapper.toConversationEntity(request, municipalityId, namespace, errandId, messageExchangeId);

		// Assert
		assertThat(conversation).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "latestSyncedSequenceNumber");
		assertThat(conversation.getErrandId()).isEqualTo(String.valueOf(errandId));
		assertThat(conversation.getMessageExchangeId()).isEqualTo(messageExchangeId);
		assertThat(conversation.getNamespace()).isEqualTo(namespace);
		assertThat(conversation.getMunicipalityId()).isEqualTo(municipalityId);
		assertThat(conversation.getTopic()).isEqualTo(topic);
		assertThat(conversation.getType()).isEqualTo(type.name());
		assertThat(conversation.getRelationIds()).containsExactlyInAnyOrderElementsOf(relationIds);
	}

	@Test
	void toConversation() {
		// Arrange
		final var conversationId = UUID.randomUUID().toString();
		final var topic = "Test Topic";
		final var type = "INTERNAL";
		final var relationIds = List.of("relation1", "relation2");
		final var entity = ConversationEntity.builder()
			.withId(conversationId)
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.build();
		final var messageExchangeId = "message-exchange-id";
		final var messageExchangeConversation = new generated.se.sundsvall.messageexchange.Conversation()
			.id(messageExchangeId)
			.topic(topic)
			.participants(List.of())
			.externalReferences(List.of())
			.metadata(List.of());

		// Act
		final var conversation = ConversationMapper.toConversation(entity, messageExchangeConversation);

		// Assert
		assertThat(conversation).isNotNull();
		assertThat(conversation.getId()).isEqualTo(entity.getId());
		assertThat(conversation.getTopic()).isEqualTo(messageExchangeConversation.getTopic());
		assertThat(conversation.getType()).isEqualTo(ConversationType.INTERNAL);
		assertThat(conversation.getRelationIds()).containsExactlyInAnyOrderElementsOf(relationIds);
		assertThat(conversation.getParticipants()).isEmpty();
		assertThat(conversation.getMetadata()).isEmpty();
	}

	@Test
	void toIdentifiers() {
		// Arrange
		final var type = "email";
		final var value = "value";
		final var identifiers = List.of(new generated.se.sundsvall.messageexchange.Identifier().type(type).value(value));

		// Act
		final var result = ConversationMapper.toIdentifiers(identifiers);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getType()).isEqualTo(type);
		assertThat(result.getFirst().getValue()).isEqualTo(value);
	}

	@Test
	void toIdentifier() {
		// Arrange
		final var type = "email";
		final var value = "value";
		final var identifier = new generated.se.sundsvall.messageexchange.Identifier().type(type).value(value);

		// Act
		final var result = ConversationMapper.toIdentifier(identifier);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getType()).isEqualTo(type);
	}

	@Test
	void toKeyValues() {
		// Arrange
		final var key = "key";
		final var values = List.of("value1", "value2");
		final var keyValues = new generated.se.sundsvall.messageexchange.KeyValues().key(key).values(values);

		// Act
		final var result = ConversationMapper.toKeyValues(keyValues);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getKey()).isEqualTo(key);
		assertThat(result.getValues()).containsExactlyInAnyOrderElementsOf(values);
	}

	@Test
	void testToKeyValues() {
		// Arrange
		final var key1 = "key1";
		final var key2 = "key2";
		final var values1 = List.of("value1");
		final var values2 = List.of("value2", "value3");
		final var keyValues = List.of(
			new generated.se.sundsvall.messageexchange.KeyValues().key(key1).values(values1),
			new generated.se.sundsvall.messageexchange.KeyValues().key(key2).values(values2));

		// Act
		final var result = ConversationMapper.toKeyValues(keyValues);

		// Assert
		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.get(0).getKey()).isEqualTo(key1);
		assertThat(result.get(0).getValues()).containsExactlyInAnyOrderElementsOf(values1);
		assertThat(result.get(1).getKey()).isEqualTo(key2);
		assertThat(result.get(1).getValues()).containsExactlyInAnyOrderElementsOf(values2);
	}

	@Test
	void toMessageExchangeKeyValues() {
		// Arrange
		final var key1 = "key1";
		final var key2 = "key2";
		final var values1 = List.of("value1");
		final var values2 = List.of("value2", "value3");
		final var keyValues = List.of(
			KeyValues.builder().withKey(key1).withValues(values1).build(),
			KeyValues.builder().withKey(key2).withValues(values2).build());

		// Act
		final var result = ConversationMapper.toMessageExchangeKeyValues(keyValues);

		// Assert
		assertThat(result).isNotNull().hasSize(2);
		assertThat(result.get(0).getKey()).isEqualTo(key1);
		assertThat(result.get(0).getValues()).containsExactlyInAnyOrderElementsOf(values1);
		assertThat(result.get(1).getKey()).isEqualTo(key2);
		assertThat(result.get(1).getValues()).containsExactlyInAnyOrderElementsOf(values2);
	}

	@Test
	void testToMessageExchangeKeyValues() {
		// Arrange
		final var key = "key";
		final var values = List.of("value1", "value2");
		final var keyValues = KeyValues.builder().withKey(key).withValues(values).build();

		// Act
		final var result = ConversationMapper.toMessageExchangeKeyValues(keyValues);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getKey()).isEqualTo(key);
		assertThat(result.getValues()).containsExactlyInAnyOrderElementsOf(values);
	}

	@Test
	void toMessageExchangeIdentifiers() {
		// Arrange
		final var type = "email";
		final var value = "value";
		final var identifiers = List.of(Identifier.builder().withType(type).withValue(value).build());

		// Act
		final var result = ConversationMapper.toMessageExchangeIdentifiers(identifiers);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getType()).isEqualTo(type);
		assertThat(result.getFirst().getValue()).isEqualTo(value);
	}

	@Test
	void toMessageExchangeIdentifier() {
		// Arrange
		final var type = "email";
		final var value = "value";
		final var identifier = Identifier.builder().withType(type).withValue(value).build();

		// Act
		final var result = ConversationMapper.toMessageExchangeIdentifier(identifier);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getType()).isEqualTo(type);
		assertThat(result.getValue()).isEqualTo(value);
	}

	@Test
	void toMessageExchangeConversation() {
		// Arrange
		final var topic = "Test Topic";
		final var type = ConversationType.INTERNAL;
		final var relationIds = List.of("relation1", "relation2");
		final var conversation = Conversation.builder()
			.withTopic(topic)
			.withType(type)
			.withRelationIds(relationIds)
			.withMetadata(List.of())
			.withParticipants(List.of())
			.build();

		// Act
		final var result = ConversationMapper.toMessageExchangeConversation(conversation);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getTopic()).isEqualTo(topic);
	}

	@Test
	void updateConversationEntity() {
		// Arrange
		final var existingId = "existing-id";
		final var existingTopic = "Existing Topic";
		final var existingType = "EXTERNAL";
		final var existingRelationIds = List.of("relation1");
		final var updatedTopic = "Updated Topic";
		final var updatedType = ConversationType.INTERNAL;
		final var updatedRelationIds = List.of("relation2", "relation3");
		final var existingEntity = ConversationEntity.builder()
			.withId(existingId)
			.withTopic(existingTopic)
			.withType(existingType)
			.withRelationIds(existingRelationIds)
			.build();
		final var request = Conversation.builder()
			.withTopic(updatedTopic)
			.withType(updatedType)
			.withRelationIds(updatedRelationIds)
			.build();

		// Act
		ConversationMapper.updateConversationEntity(existingEntity, request);

		// Assert
		assertThat(existingEntity).isNotNull();
		assertThat(existingEntity.getId()).isEqualTo(existingId);
		assertThat(existingEntity.getTopic()).isEqualTo(updatedTopic);
		assertThat(existingEntity.getType()).isEqualTo(updatedType.name());
		assertThat(existingEntity.getRelationIds()).containsExactlyInAnyOrderElementsOf(updatedRelationIds);
	}

	@Test
	void toMessageRequest() {
		// Arrange
		final var content = "Test message";
		final var inReplyToMessageId = "reply-id";
		final var message = se.sundsvall.casedata.api.model.conversation.Message.builder()
			.withContent(content)
			.withInReplyToMessageId(inReplyToMessageId)
			.build();

		// Act
		final var result = ConversationMapper.toMessageRequest(message);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getContent()).isEqualTo(content);
		assertThat(result.getInReplyToMessageId()).isEqualTo(inReplyToMessageId);
	}

	@Test
	void toMessagePage() {
		// Arrange
		final var message1 = new generated.se.sundsvall.messageexchange.Message().id("1").content("Message 1").inReplyToMessageId("0");
		final var message2 = new generated.se.sundsvall.messageexchange.Message().id("2").content("Message 2").inReplyToMessageId("1");
		final var messages = List.of(message1, message2);
		final var page = new org.springframework.data.domain.PageImpl<>(messages);

		// Act
		final var result = ConversationMapper.toMessagePage(page);

		// Assert
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().getFirst().getId()).isEqualTo("1");
		assertThat(result.getContent().getFirst().getContent()).isEqualTo("Message 1");
		assertThat(result.getContent().getFirst().getInReplyToMessageId()).isEqualTo("0");
		assertThat(result.getContent().getLast().getId()).isEqualTo("2");
		assertThat(result.getContent().getLast().getContent()).isEqualTo("Message 2");
		assertThat(result.getContent().getLast().getInReplyToMessageId()).isEqualTo("1");
	}

	@Test
	void toReadBy() {
		// Arrange
		final var type = "email";
		final var value = "value";
		final var readAt = OffsetDateTime.now().minusDays(7);
		final var identifier = new generated.se.sundsvall.messageexchange.Identifier().type(type).value(value);
		final var readBy = List.of(new generated.se.sundsvall.messageexchange.ReadBy().identifier(identifier).readAt(readAt));

		// Act
		final var result = ConversationMapper.toReadBy(readBy);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst().getIdentifier().getType()).isEqualTo(type);
		assertThat(result.getFirst().getIdentifier().getValue()).isEqualTo(value);
		assertThat(result.getFirst().getReadAt()).isCloseTo(readAt, within(5, SECONDS));
	}

	@Test
	void toAttachments() {
		// Arrange
		final var attachmentId = "attachment-id";
		final var fileName = "attachment-name";
		final var mimeType = "application/pdf";
		final var fileSize = 123;
		final var attachments = List.of(
			new generated.se.sundsvall.messageexchange.Attachment()
				.id(attachmentId)
				.fileName(fileName)
				.mimeType(mimeType)
				.fileSize(fileSize));

		// Act
		final var result = ConversationMapper.toAttachments(attachments);

		// Assert
		assertThat(result).isNotNull().hasSize(1);
		assertThat(result.getFirst()).hasNoNullFieldsOrPropertiesExcept("created");
		assertThat(result.getFirst().getId()).isEqualTo(attachmentId);
		assertThat(result.getFirst().getFileName()).isEqualTo(fileName);
		assertThat(result.getFirst().getMimeType()).isEqualTo(mimeType);
		assertThat(result.getFirst().getFileSize()).isEqualTo(fileSize);
	}

	@Test
	void toConversationList() {
		// Arrange
		final var conversationId = UUID.randomUUID().toString();
		final var topic = "Test Topic";
		final var type = ConversationType.INTERNAL;
		final var relationIds = List.of("relation1", "relation2");
		final var entity = ConversationEntity.builder()
			.withId(conversationId)
			.withTopic(topic)
			.withType(type.name())
			.withRelationIds(relationIds)
			.build();

		// Act
		final var conversations = ConversationMapper.toConversationList(List.of(entity));

		// Assert
		assertThat(conversations).isNotNull().hasSize(1);
		assertThat(conversations.getFirst().getId()).isEqualTo(conversationId);
		assertThat(conversations.getFirst().getTopic()).isEqualTo(topic);
		assertThat(conversations.getFirst().getType()).isEqualTo(type);
		assertThat(conversations.getFirst().getRelationIds()).containsExactlyInAnyOrderElementsOf(relationIds);
	}

	@Test
	void toConversationListEmptyList() {
		// Act
		final var conversations = ConversationMapper.toConversationList(emptyList());

		// Assert
		assertThat(conversations).isNotNull().isEmpty();
	}

	@Test
	void toConversationListNullList() {
		// Act
		final var conversations = ConversationMapper.toConversationList(null);

		// Assert
		assertThat(conversations).isNotNull().isEmpty();
	}

	@Test
	void toConversationFromConversationEntity() {
		// Arrange
		final var conversationId = UUID.randomUUID().toString();
		final var topic = "Test Topic";
		final var type = ConversationType.INTERNAL;
		final var relationIds = List.of("relation1", "relation2");
		final var entity = ConversationEntity.builder()
			.withId(conversationId)
			.withTopic(topic)
			.withType(type.name())
			.withRelationIds(relationIds)
			.build();

		// Act
		final var conversation = ConversationMapper.toConversation(entity);

		// Assert
		assertThat(conversation).isNotNull().hasNoNullFieldsOrPropertiesExcept("participants", "metadata");
		assertThat(conversation.getId()).isEqualTo(conversationId);
		assertThat(conversation.getTopic()).isEqualTo(topic);
		assertThat(conversation.getType()).isEqualTo(type);
		assertThat(conversation.getRelationIds()).containsExactlyInAnyOrderElementsOf(relationIds);
		assertThat(conversation.getParticipants()).isNull();
		assertThat(conversation.getMetadata()).isNull();
	}

	@Test
	void toConversationFromConversationEntityNullValues() {
		// Arrange
		final var entity = ConversationEntity.builder().build();

		// Act
		final var conversation = ConversationMapper.toConversation(entity);

		// Assert
		assertThat(conversation).isNotNull().hasAllNullFieldsOrProperties();
	}

	@Test
	void toConversationFromConversationEntityNull() {
		// Act
		final var conversation = ConversationMapper.toConversation(null);

		// Assert
		assertThat(conversation).isNull();
	}

	@Test
	void toAttachment() {
		// Arrange
		final var fileName = "attachment-name";
		final var mimeType = "application/pdf";
		final var fileSize = 123;
		final var errandId = 12345L;
		final var municipalityId = "2281";
		final var namespace = "namespace";

		final var attachment = new MockMultipartFile("attachment", fileName, mimeType, new byte[fileSize]);

		// Act
		final var result = ConversationMapper.toAttachment(attachment, errandId, municipalityId, namespace);

		// Assert
		assertThat(result).isNotNull().hasNoNullFieldsOrPropertiesExcept("id", "created", "updated", "category", "note", "extension");
		assertThat(result.getName()).isEqualTo(fileName);
		assertThat(result.getMimeType()).isEqualTo(mimeType);
	}

	@Test
	void toAttachmentThrowsIOException() throws IOException {
		// Arrange
		final var attachment = spy(new MockMultipartFile("attachment", "attachment-name", "application/pdf", new byte[0]));
		final var errandId = 12345L;
		final var municipalityId = "2281";
		final var namespace = "namespace";

		when(attachment.getBytes()).thenThrow(new IOException("Failed to read attachment content"));

		// Act & Assert
		assertThatThrownBy(() -> ConversationMapper.toAttachment(attachment, errandId, municipalityId, namespace))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Failed to read attachment content");
	}

}
