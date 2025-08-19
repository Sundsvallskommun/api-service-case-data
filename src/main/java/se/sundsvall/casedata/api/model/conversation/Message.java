package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Message model")
public class Message {

	@Schema(description = "Message ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506", accessMode = Schema.AccessMode.READ_ONLY)
	private String id;

	@ValidUuid(nullable = true)
	@Schema(description = "The ID of the replied message", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506")
	private String inReplyToMessageId;

	@Schema(description = "The timestamp when the message was created.", example = "2023-01-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
	private OffsetDateTime created;

	@Schema(description = "The participant who created the message.", accessMode = Schema.AccessMode.READ_ONLY)
	private Identifier createdBy;

	@NotBlank
	@Schema(description = "The content of the message.", example = "Hello, how can I help you?")
	private String content;

	@ArraySchema(schema = @Schema(implementation = ReadBy.class, description = "A list of users who have read the message.", accessMode = Schema.AccessMode.READ_ONLY))
	private List<ReadBy> readBy;

	@ArraySchema(schema = @Schema(implementation = ConversationAttachment.class, description = "A list of attachments associated with the message.", accessMode = Schema.AccessMode.READ_ONLY))
	private List<ConversationAttachment> attachments;

	@Schema(description = "Type of message (user or system created)", example = "USER_CREATED", accessMode = Schema.AccessMode.READ_ONLY)
	private MessageType type;
}
