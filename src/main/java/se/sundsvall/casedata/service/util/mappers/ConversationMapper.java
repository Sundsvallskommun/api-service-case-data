package se.sundsvall.casedata.service.util.mappers;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.conversation.Attachment;
import se.sundsvall.casedata.api.model.conversation.Conversation;
import se.sundsvall.casedata.api.model.conversation.ConversationType;
import se.sundsvall.casedata.api.model.conversation.Identifier;
import se.sundsvall.casedata.api.model.conversation.KeyValues;
import se.sundsvall.casedata.api.model.conversation.Message;
import se.sundsvall.casedata.api.model.conversation.ReadBy;
import se.sundsvall.casedata.integration.db.model.ConversationEntity;

public final class ConversationMapper {

	public static final String RELATION_ID_KEY = "relationIds";

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
			.withType(Optional.ofNullable(request.getType()).map(ConversationType::name).orElse(null))
			.withRelationIds(request.getRelationIds())
			.build();
	}

	public static Conversation toConversation(final ConversationEntity entity, final generated.se.sundsvall.messageexchange.Conversation conversation) {
		return Conversation.builder()
			.withId(entity.getId())
			.withTopic(conversation.getTopic())
			.withType(Optional.ofNullable(entity.getType()).map(ConversationType::valueOf).orElse(null))
			.withRelationIds(Optional.ofNullable(entity.getRelationIds()).map(ArrayList::new).orElse(null))
			.withParticipants(toIdentifiers(conversation.getParticipants()))
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
			.externalReferences(toMessageExchangeKeyValues(List.of(KeyValues.builder().withKey(RELATION_ID_KEY).withValues(conversation.getRelationIds()).build())))
			.metadata(toMessageExchangeKeyValues(conversation.getMetadata()))
			.participants(toMessageExchangeIdentifiers(conversation.getParticipants()))
			.topic(conversation.getTopic());

	}

	public static void updateConversationEntity(final ConversationEntity conversationEntity, final Conversation request) {
		Optional.ofNullable(request.getTopic()).ifPresent(conversationEntity::setTopic);
		Optional.ofNullable(request.getType()).ifPresent(type -> conversationEntity.setType(type.name()));
		Optional.ofNullable(request.getRelationIds()).ifPresent(conversationEntity::setRelationIds);

	}

	public static ConversationEntity updateConversationEntity(final ConversationEntity conversationEntity, final generated.se.sundsvall.messageexchange.Conversation conversation) {
		Optional.ofNullable(conversation)
			.ifPresent(c -> {
				conversationEntity.setLatestSyncedSequenceNumber(c.getLatestSequenceNumber());
				conversationEntity.setTopic(c.getTopic());
				conversationEntity.setRelationIds(toStringList(toKeyValues(c.getExternalReferences()), RELATION_ID_KEY));
			});
		return conversationEntity;
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
			.withCreatedBy(Optional.ofNullable(message.getCreatedBy()).map(ConversationMapper::toIdentifier).orElse(null))
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
				.withFileSize(Optional.ofNullable(attachment.getFileSize()).orElse(0))
				.build())
			.toList();
	}

	public static List<Conversation> toConversationList(final List<ConversationEntity> conversationEntityList) {
		return Optional.ofNullable(conversationEntityList).orElse(emptyList()).stream()
			.map(ConversationMapper::toConversation)
			.toList();
	}

	public static Conversation toConversation(final ConversationEntity conversationEntity) {
		return Optional.ofNullable(conversationEntity)
			.map(c -> Conversation.builder()
				.withId(c.getId())
				.withRelationIds(Optional.ofNullable(c.getRelationIds()).map(ArrayList::new).orElse(null))
				.withTopic(c.getTopic())
				.withType(Optional.ofNullable(c.getType()).map(ConversationType::valueOf).orElse(null))
				.build())
			.orElse(null);
	}

	private static List<String> toStringList(final List<KeyValues> keyValueList, final String key) {
		return new ArrayList<>(Optional.ofNullable(keyValueList).orElse(Collections.emptyList()).stream()
			.filter(keyValues -> equalsIgnoreCase(keyValues.getKey(), key))
			.flatMap(keyValues -> keyValues.getValues().stream())
			.toList());
	}

	public static se.sundsvall.casedata.api.model.Attachment toAttachment(final MultipartFile attachment, final Long errandId, final String municipalityId, final String namespace) {

		final String contentString;
		try {
			contentString = Optional.of(attachment.getBytes())
				.map(ConversationMapper::toContentString)
				.orElse(null);
		} catch (final IOException e) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to read attachment content");
		}

		return se.sundsvall.casedata.api.model.Attachment.builder()
			.withErrandId(errandId)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withFile(contentString)
			.withName(attachment.getOriginalFilename())
			.withMimeType(attachment.getContentType())
			.build();
	}

	public static se.sundsvall.casedata.api.model.Attachment toAttachment(final byte[] content, final String filename, final String mimeType, final Long errandId, final String municipalityId, final String namespace) {

		final String contentString = Optional.ofNullable(content)
			.map(ConversationMapper::toContentString)
			.orElse(null);

		return se.sundsvall.casedata.api.model.Attachment.builder()
			.withErrandId(errandId)
			.withMunicipalityId(municipalityId)
			.withNamespace(namespace)
			.withFile(contentString)
			.withName(filename)
			.withMimeType(mimeType)
			.build();
	}

	private static String toContentString(final byte[] result) {
		return new String(Base64.getEncoder().encode(result), UTF_8);
	}

}
