package se.sundsvall.casedata.service.util.mappers;

import static java.util.Collections.emptyList;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import se.sundsvall.casedata.api.model.conversation.Attachment;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.ConversationType;
import se.sundsvall.casedata.api.model.conversation.Identifier;
import se.sundsvall.casedata.api.model.conversation.KeyValues;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.api.model.conversation.ReadBy;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;

public final class ConversationMapper {

	private ConversationMapper() {
		// Private constructor to prevent instantiation
	}

	public static ConversationEntity toConversationEntity(final Conversation request, final String municipalityId, final String namespace, final Long errandId, final String messageExchangeId) {
		return ConversationEntity.builder()
			.withErrandId(Optional.ofNullable(errandId).orElse(0L).toString())
			.withMessageExchangeId(messageExchangeId)
			.withNamespace(namespace)
			.withMunicipalityId(municipalityId)
			.withTopic(request.getTopic())
			.withType(request.getType() != null ? request.getType().name() : null)
			.withRelationIds(request.getRelationIds())
			.build();
	}

	public static Conversation toConversation(final ConversationEntity entity, final generated.se.sundsvall.messageexchange.Conversation conversation) {
		return Conversation.builder()
			.withId(entity.getId())
			.withTopic(conversation.getTopic())
			.withType(entity.getType() != null ? ConversationType.valueOf(entity.getType()) : null)
			.withRelationIds(entity.getRelationIds())
			.withParticipants(toIdentifiers(conversation.getParticipants()))
			.withExternalReferences(toKeyValues(conversation.getExternalReferences()))
			.withMetadata(toKeyValues(conversation.getMetadata()))
			.build();
	}

	public static List<Identifier> toIdentifiers(final List<generated.se.sundsvall.messageexchange.Identifier> identifiers) {
		return identifiers.stream()
			.map(ConversationMapper::toIdentifier)
			.toList();
	}

	public static Identifier toIdentifier(final generated.se.sundsvall.messageexchange.Identifier identifier) {
		if (identifier == null) {
			return null;
		}
		return Identifier.builder()
			.withType(identifier.getType())
			.withValue(identifier.getValue())
			.build();
	}

	public static List<KeyValues> toKeyValues(final List<generated.se.sundsvall.messageexchange.KeyValues> keyValuesList) {
		return keyValuesList.stream()
			.map(ConversationMapper::toKeyValues)
			.toList();
	}

	public static KeyValues toKeyValues(final generated.se.sundsvall.messageexchange.KeyValues keyValues) {
		return KeyValues.builder()
			.withKey(keyValues.getKey())
			.withValues(keyValues.getValues())
			.build();
	}

	public static List<generated.se.sundsvall.messageexchange.KeyValues> toMessageExchangeKeyValues(final List<KeyValues> keyValuesList) {
		return Optional.ofNullable(keyValuesList).orElse(emptyList()).stream()
			.map(ConversationMapper::toMessageExchangeKeyValues)
			.toList();
	}

	public static generated.se.sundsvall.messageexchange.KeyValues toMessageExchangeKeyValues(final KeyValues keyValues) {
		return new generated.se.sundsvall.messageexchange.KeyValues()
			.key(keyValues.getKey())
			.values(keyValues.getValues());
	}

	public static List<generated.se.sundsvall.messageexchange.Identifier> toMessageExchangeIdentifiers(final List<Identifier> identifiers) {
		return Optional.ofNullable(identifiers)
			.orElse(emptyList())
			.stream()
			.map(ConversationMapper::toMessageExchangeIdentifier)
			.toList();
	}

	public static generated.se.sundsvall.messageexchange.Identifier toMessageExchangeIdentifier(final Identifier identifier) {
		return new generated.se.sundsvall.messageexchange.Identifier()
			.type(identifier.getType())
			.value(identifier.getValue());
	}

	public static generated.se.sundsvall.messageexchange.Conversation toMessageExchangeConversation(final Conversation conversation) {
		return new generated.se.sundsvall.messageexchange.Conversation()
			.externalReferences(toMessageExchangeKeyValues(conversation.getExternalReferences()))
			.metadata(toMessageExchangeKeyValues(conversation.getMetadata()))
			.participants(toMessageExchangeIdentifiers(conversation.getParticipants()))
			.topic(conversation.getTopic());

	}

	public static void updateConversationEntity(final ConversationEntity conversationEntity, final Conversation request) {
		Optional.ofNullable(request.getTopic()).ifPresent(conversationEntity::setTopic);
		Optional.ofNullable(request.getType()).ifPresent(type -> conversationEntity.setType(type.name()));
		Optional.ofNullable(request.getRelationIds()).ifPresent(conversationEntity::setRelationIds);

	}

	public static generated.se.sundsvall.messageexchange.Message toMessageRequest(final Message messageRequest) {
		return new generated.se.sundsvall.messageexchange.Message()
			.inReplyToMessageId(messageRequest.getInReplyToMessageId())
			.content(messageRequest.getContent());
	}

	public static Page<Message> toMessagePage(final Page<generated.se.sundsvall.messageexchange.Message> body) {
		return body.map(message -> Message.builder()
			.withId(message.getId())
			.withInReplyToMessageId(message.getInReplyToMessageId())
			.withCreated(message.getCreated())
			.withCreatedBy(message.getCreatedBy() == null ? null : toIdentifier(message.getCreatedBy()))
			.withContent(message.getContent())
			.withReadBy(ConversationMapper.toReadBy(message.getReadBy()))
			.withAttachments(ConversationMapper.toAttachments(message.getAttachments()))
			.build());
	}

	public static List<ReadBy> toReadBy(final List<generated.se.sundsvall.messageexchange.ReadBy> readBy) {
		return readBy.stream()
			.map(mappedReadby -> ReadBy.builder()
				.withReadAt(mappedReadby.getReadAt())
				.withIdentifier(toIdentifier(mappedReadby.getIdentifier()))
				.build())
			.toList();
	}

	static List<Attachment> toAttachments(@Valid final List<generated.se.sundsvall.messageexchange.@Valid Attachment> attachments) {
		return attachments.stream()
			.map(attachment -> Attachment.builder()
				.withId(attachment.getId())
				.withFileName(attachment.getFileName())
				.withMimeType(attachment.getMimeType())
				.withFileSize(attachment.getFileSize() != null ? attachment.getFileSize() : 0)
				.build())
			.toList();
	}
}
