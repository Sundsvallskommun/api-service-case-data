package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.Size;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import se.sundsvall.casedata.api.model.validation.ValidMapValueSize;
import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class PatchDecision {

	@Schema(description = "The type of decision", example = "APPROVAL")
	private DecisionType decisionType;

	@Schema(description = "The outcome of the decision", example = "GRANTED")
	private DecisionOutcome decisionOutcome;

	@Schema(description = "Description of the decision", example = "The application has been approved.", maxLength = 1000)
	@Size(max = 1000)
	private String description;

	@Schema(description = "The date and time when the decision was made", example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime decidedAt;

	@Schema(description = "The date and time when the decision becomes valid", example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validFrom;

	@Schema(description = "The date and time when the decision expires", example = "2023-12-31T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validTo;

	@Schema(description = "Additional parameters for the decision", example = "{\"key1\":\"value1\",\"key2\":\"value2\"}")
	@ValidMapValueSize(max = 8192)
	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
