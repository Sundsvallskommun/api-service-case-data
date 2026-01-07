package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.casedata.api.model.validation.ValidFacilityType;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Facility {

	@Schema(description = "The id of the facility", accessMode = READ_ONLY, examples = "1")
	private Long id;

	@Schema(description = "The version of the facility", accessMode = READ_ONLY, examples = "1")
	private int version;

	@Schema(description = "The municipality ID", examples = "2281", accessMode = READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", examples = "MY_NAMESPACE", accessMode = READ_ONLY)
	private String namespace;

	@Schema(description = "Description of the facility", examples = "En fritextbeskrivning av facility.", maxLength = 255)
	@Size(max = 255)
	private String description;

	@Valid
	@Schema(description = "The address of the facility")
	private Address address;

	@Schema(description = "The name on the sign", examples = "Sundsvalls testfabrik", maxLength = 255)
	@Size(max = 255)
	private String facilityCollectionName;

	@Schema(description = "Is this the main facility for the case?", examples = "true")
	private boolean mainFacility;

	@Schema(description = "Type of the facility", examples = "INDUSTRIAL")
	@ValidFacilityType
	private String facilityType;

	@Schema(description = "Date and time when the facility was created", accessMode = Schema.AccessMode.READ_ONLY, examples = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "Date and time when the facility was last updated", accessMode = Schema.AccessMode.READ_ONLY, examples = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@ValidMapValueSize(max = 8192)
	@Schema(description = "Extra parameters", examples = "{\"key1\":\"value1\",\"key2\":\"value2\"}")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
