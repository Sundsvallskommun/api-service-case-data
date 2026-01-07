package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Related errand for errand")
public class RelatedErrand {

	@Schema(description = "Errand id", examples = "123")
	private Long errandId;

	@NotNull
	@Schema(description = "Errand number", examples = "PRH-2022-000001")
	private String errandNumber;

	@Schema(description = "Relation reason", examples = "Related because of appealed decision on errand")
	private String relationReason;
}
