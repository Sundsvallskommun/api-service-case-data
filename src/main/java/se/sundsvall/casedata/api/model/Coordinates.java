package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Coordinates {

	@Schema(description = "Decimal Degrees (DD)", examples = "62.390205")
	private Double latitude;

	@Schema(description = "Decimal Degrees (DD)", examples = "17.306616")
	private Double longitude;

}
