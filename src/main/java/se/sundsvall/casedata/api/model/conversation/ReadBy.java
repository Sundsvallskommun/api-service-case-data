package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Readby model")
public class ReadBy {

	@Schema(description = "The identifier of the person who read the message.", example = "joe01doe")
	private Identifier identifier;

	@Schema(description = "The timestamp when the message was read.", example = "2023-01-01T12:00:00+01:00")
	private OffsetDateTime readAt;

}
