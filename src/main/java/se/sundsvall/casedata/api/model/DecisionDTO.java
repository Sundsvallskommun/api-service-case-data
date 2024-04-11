package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import lombok.experimental.SuperBuilder;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class DecisionDTO extends BaseDTO {

	private DecisionType decisionType;

	private DecisionOutcome decisionOutcome;

	@Size(max = 100000)
	private String description;

	@Builder.Default
	private List<LawDTO> law = new ArrayList<>();

	private StakeholderDTO decidedBy;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime decidedAt;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validFrom;

	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime validTo;

	@Builder.Default
	private List<AttachmentDTO> attachments = new ArrayList<>();

	@Builder.Default
	private Map<String, String> extraParameters = new HashMap<>();

}
