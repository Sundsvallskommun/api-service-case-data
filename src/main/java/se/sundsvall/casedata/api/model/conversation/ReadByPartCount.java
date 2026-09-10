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
@Schema(description = "Number of messages read by a specific part")
public class ReadByPartCount {

	@Schema(description = "The part the count refers to", example = "KC-23010001")
	private String part;

	@Schema(description = "Number of messages read by the part")
	private Long count;

}
