package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casedata.api.model.enums.FacilityType;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class FacilityDTO extends BaseDTO {

	@Schema(example = "En fritextbeskrivning av facility.")
	@Size(max = 255)
	private String description;

	@Valid
	private AddressDTO address;

	@Schema(description = "The name on the sign.", example = "Sundsvalls testfabrik")
	@Size(max = 255)
	private String facilityCollectionName;

	private boolean mainFacility;

	private FacilityType facilityType;

	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
