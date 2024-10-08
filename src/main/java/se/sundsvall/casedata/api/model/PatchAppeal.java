package se.sundsvall.casedata.api.model;

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

@Data
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class PatchAppeal {

	@Size(max = 100000)
	@Schema(description = "Description of the appeal", example = "The decision is not correct")
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
