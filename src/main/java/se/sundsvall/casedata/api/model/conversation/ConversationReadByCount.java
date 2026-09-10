package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Read-by statistics for a conversation")
public class ConversationReadByCount {

	@Schema(description = "The SM conversation ID", example = "896a44d8-724b-11ed-a840-0242ac110002")
	private String conversationId;

	@Schema(description = "Total number of messages in the conversation")
	private Long messageCount;

	@ArraySchema(schema = @Schema(implementation = ReadByCount.class, description = "Number of messages read per identifier"))
	private List<ReadByCount> readByCount;

	@ArraySchema(schema = @Schema(implementation = ReadByPartCount.class, description = "Number of messages read per part"))
	private List<ReadByPartCount> readByPartCount;

}
