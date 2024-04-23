package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import se.sundsvall.casedata.api.model.validation.ValidAppealStatus;
import se.sundsvall.casedata.api.model.validation.ValidTimelinessReviewValue;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(setterPrefix = "with")
public class PatchAppealDTO extends BaseDTO {

	@Size(max = 100000)
	private String description;

	@Schema(description = "Current status for this appeal. Values [NEW, REJECTED, SENT_TO_COURT, COMPLETED]", defaultValue = "NEW")
	@ValidAppealStatus
	@Builder.Default
	private String status = "NEW";

	@Schema(description = "Status of whether measures have been taken within statutory time limits. Values: [NOT_CONDUCTED, NOT_RELEVANT, APPROVED, REJECTED]", defaultValue = "NOT_CONDUCTED")
	@ValidTimelinessReviewValue
	@Builder.Default
	private String timelinessReview = "NOT_CONDUCTED";
}
