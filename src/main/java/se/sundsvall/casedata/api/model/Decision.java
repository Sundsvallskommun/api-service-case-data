package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Decision {

	@Schema(description = "The id of the decision", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private Long id;

	@Schema(description = "The version of the decision", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private int version;

	@Schema(description = "The municipality ID", example = "2281", accessMode = Schema.AccessMode.READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", example = "my.namespace", accessMode = Schema.AccessMode.READ_ONLY)
	private String namespace;

	@Schema(description = "Type of the decision", example = "APPROVAL")
	private DecisionType decisionType;

	@Schema(description = "Outcome of the decision", example = "GRANTED")
	private DecisionOutcome decisionOutcome;

	@Schema(description = "Description of the decision", example = "This decision approves the application.")
	@Size(max = 100000)
	private String description;

	@Schema(description = "List of laws related to the decision")
	private List<Law> law;

	@Schema(description = "Stakeholder who made the decision")
	private Stakeholder decidedBy;

	@Schema(description = "Date and time when the decision was made", example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime decidedAt;

	@Schema(description = "Date and time when the decision becomes valid", example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validFrom;

	@Schema(description = "Date and time when the decision expires", example = "2024-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validTo;

	@Schema(description = "List of attachments related to the decision")
	private List<Attachment> attachments;

	@Schema(description = "Additional parameters for the decision", example = "{\"key1\": \"value1\", \"key2\": \"value2\"}")
	@ValidMapValueSize(max = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Schema(description = "Date and time when the decision was created", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "Date and time when the decision was last updated", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

}
