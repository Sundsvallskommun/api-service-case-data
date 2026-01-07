package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Identifier model")
public class Identifier {

	@Pattern(regexp = "^(adAccount|partyId)$", message = "Type must be 'adAccount' or 'partyId'")
	@Schema(description = "The conversation identifier type", examples = "adAccount")
	private String type;

	@NotBlank
	@Schema(description = "The conversation identifier value", examples = "joe01doe")
	private String value;

}
