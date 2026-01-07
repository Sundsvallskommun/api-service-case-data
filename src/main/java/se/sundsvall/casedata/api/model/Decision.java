package se.sundsvall.casedata.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

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
import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Decision {

	@Schema(description = "The id of the decision", accessMode = READ_ONLY, examples = "1")
	private Long id;

	@Schema(description = "The version of the decision", accessMode = READ_ONLY, examples = "1")
	private int version;

	@Schema(description = "The municipality ID", examples = "2281", accessMode = READ_ONLY)
	private String municipalityId;

	@Schema(description = "Namespace", examples = "MY_NAMESPACE", accessMode = READ_ONLY)
	private String namespace;

	@Schema(description = "Type of the decision", examples = "APPROVAL")
	private DecisionType decisionType;

	@Schema(description = "Outcome of the decision", examples = "GRANTED")
	private DecisionOutcome decisionOutcome;

	@Schema(description = "Description of the decision", examples = "This decision approves the application.", maxLength = 100000)
	@Size(max = 100000)
	private String description;

	@Schema(description = "List of laws related to the decision")
	private List<Law> law;

	@Schema(description = "Stakeholder who made the decision")
	private Stakeholder decidedBy;

	@Schema(description = "Date and time when the decision was made", examples = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime decidedAt;

	@Schema(description = "Date and time when the decision becomes valid", examples = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime validFrom;

	@Schema(description = "Date and time when the decision expires", examples = "2024-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime validTo;

	@Schema(description = "List of attachments related to the decision")
	private List<Attachment> attachments;

	@Schema(description = "Additional parameters for the decision", examples = "{\"key1\": \"value1\", \"key2\": \"value2\"}")
	@ValidMapValueSize(max = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

	@Schema(description = "Date and time when the decision was created", accessMode = READ_ONLY, examples = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "Date and time when the decision was last updated", accessMode = READ_ONLY, examples = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

}
