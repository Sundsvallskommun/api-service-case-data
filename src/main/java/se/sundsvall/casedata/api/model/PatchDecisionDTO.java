package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.validation.constraints.Size;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import se.sundsvall.casedata.integration.db.model.enums.DecisionOutcome;
import se.sundsvall.casedata.integration.db.model.enums.DecisionType;

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
public class PatchDecisionDTO {

	private DecisionType decisionType;

	private DecisionOutcome decisionOutcome;

	@Size(max = 1000)
	private String description;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime decidedAt;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validFrom;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validTo;

	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
