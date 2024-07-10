package se.sundsvall.casedata.api.model;

import java.time.OffsetDateTime;

import jakarta.validation.constraints.Size;

import se.sundsvall.casedata.api.model.validation.ValidAppealStatus;
import se.sundsvall.casedata.api.model.validation.ValidTimelinessReviewValue;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class AppealDTO extends BaseDTO {

	@Size(max = 100000)
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
}
