package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Message model")
public class Message {

	@Schema(description = "Message ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506")
	private String id;

	@Schema(description = "The ID of the replied message", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506")
	private String inReplyToMessageId;

	@Schema(description = "The timestamp when the message was created.", example = "2023-01-01T12:00:00")
	private OffsetDateTime created;

	@Schema(description = "The participant who created the message.")
	private Identifier createdBy;

	@Schema(description = "The content of the message.", example = "Hello, how can I help you?")
	private String content;

	@ArraySchema(schema = @Schema(implementation = ReadBy.class, description = "A list of users who have read the message."))
	private List<ReadBy> readBy;

	@ArraySchema(schema = @Schema(implementation = Attachment.class, description = "A list of attachments associated with the message."))
	private List<Attachment> attachments;

}
