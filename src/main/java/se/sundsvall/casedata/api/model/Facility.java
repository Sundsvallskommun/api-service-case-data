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
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import se.sundsvall.casedata.api.model.validation.ValidFacilityType;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Facility {

	@Schema(description = "The id of the facility", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private long id;

	@Schema(description = "The version of the facility", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private int version;

	@Schema(description = "The municipality ID", example = "2281", accessMode = Schema.AccessMode.READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", example = "my.namespace", accessMode = Schema.AccessMode.READ_ONLY)
	private String namespace;

	@Schema(description = "Description of the facility", example = "En fritextbeskrivning av facility.")
	@Size(max = 255)
	private String description;

	@Valid
	@Schema(description = "The address of the facility")
	private Address address;

	@Schema(description = "The name on the sign", example = "Sundsvalls testfabrik")
	@Size(max = 255)
	private String facilityCollectionName;

	@Schema(description = "Is this the main facility for the case?", example = "true")
	private boolean mainFacility;

	@Schema(description = "Type of the facility", example = "INDUSTRIAL")
	@ValidFacilityType
	private String facilityType;

	@Schema(description = "Date and time when the facility was created", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "Date and time when the facility was last updated", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

	@ValidMapValueSize(max = 8192)
	@Schema(description = "Extra parameters", example = "{\"key1\":\"value1\",\"key2\":\"value2\"}")
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
