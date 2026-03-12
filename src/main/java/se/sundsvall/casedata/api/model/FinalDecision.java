package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class FinalDecision {

	@Schema(description = "The id of the errand", accessMode = READ_ONLY, examples = "1")
	private Long errandId;

	@Schema(description = "Errand number", examples = "SGP-2022-000001", accessMode = READ_ONLY)
	private String errandNumber;

	@Schema(description = "The final decision", examples = "APPROVAL")
	private Decision decision;
}
