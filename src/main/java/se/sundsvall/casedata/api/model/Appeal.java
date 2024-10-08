package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Size;

import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import se.sundsvall.casedata.api.model.validation.ValidAppealStatus;
import se.sundsvall.casedata.api.model.validation.ValidTimelinessReviewValue;

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
public class Appeal {

	@Schema(description = "The id of the appeal", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private long id;

	@Schema(description = "The version of the appeal.", accessMode = Schema.AccessMode.READ_ONLY, example = "1")
	private int version;

	@Size(max = 100000)
	@Schema(description = "Description of the appeal", example = "Some description of the appeal.")
	private String description;

	@Schema(description = "The date when this appeal was first registered (timestamp from e-service, mail or letter)")
	private OffsetDateTime registeredAt;

	@Schema(description = "The date when the decision or corresponding that this appeal concerns was sent out")
	private OffsetDateTime appealConcernCommunicatedAt;

	@Schema(description = "Current status for this appeal. Values [NEW, REJECTED, SENT_TO_COURT, COMPLETED]", defaultValue = "NEW")
	@ValidAppealStatus
	@Builder.Default
	private String status = "NEW";

	@Schema(description = "Status of whether measures have been taken within statutory time limits. Values: [NOT_CONDUCTED, NOT_RELEVANT, APPROVED, REJECTED]", defaultValue = "NOT_CONDUCTED")
	@ValidTimelinessReviewValue
	@Builder.Default
	private String timelinessReview = "NOT_CONDUCTED";

	@Schema(description = "Id for decision that is appealed", nullable = true)
	private Long decisionId;

	@Schema(description = "The date when this appeal was created", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-01T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime created;

	@Schema(description = "The date when this appeal was last updated", accessMode = Schema.AccessMode.READ_ONLY, example = "2023-10-02T12:00:00Z")
	@TimeZoneStorage(TimeZoneStorageType.NORMALIZE)
	private OffsetDateTime updated;

}
