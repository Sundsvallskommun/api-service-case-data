package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Conversation model")
public class Conversation {

	@Schema(description = "Conversation ID", example = "1aefbbb8-de82-414b-b5d7-ba7c5bbe4506", accessMode = Schema.AccessMode.READ_ONLY)
	private String id;

	@NotBlank
	@Schema(description = "The message-exchange topic", example = "The conversation topic")
	private String topic;

	@NotNull
	@Schema(description = "The conversation type", example = "INTERNAL")
	private ConversationType type;

	@ArraySchema(schema = @Schema(implementation = String.class, description = "List with relation ID:s"))
	private List<@ValidUuid String> relationIds;

	@ArraySchema(schema = @Schema(implementation = Identifier.class, description = "A list of participants in this conversation"))
	private List<@Valid Identifier> participants;

	@ArraySchema(schema = @Schema(implementation = KeyValues.class, description = "A list of metadata objects associated with the conversation"))
	private List<@Valid KeyValues> metadata;

}
