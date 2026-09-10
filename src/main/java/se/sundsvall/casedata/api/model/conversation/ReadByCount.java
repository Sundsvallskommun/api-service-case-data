package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Number of messages read by a specific identifier")
public class ReadByCount {

	@Schema(description = "The identifier of the person the count refers to")
	private Identifier identifier;

	@Schema(description = "Number of messages read by the identifier")
	private Long count;

}
