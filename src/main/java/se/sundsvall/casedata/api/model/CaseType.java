package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class CaseType {

	@Schema(description = "The case type", examples = "PARATRANSIT")
	private String type;

	@Schema(description = "The display name of the case type", examples = "Färdtjänst")
	private String displayName;
}
