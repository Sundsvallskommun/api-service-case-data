package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class GetParkingPermit {

	@Schema(description = "The permit number of the artefact", example = "PARK123456")
	private String artefactPermitNumber;

	@Schema(description = "The status of the artefact permit", example = "ACTIVE")
	private String artefactPermitStatus;

	@Schema(description = "The ID of the associated errand", example = "1")
	private Long errandId;

	@Schema(description = "The decision related to the errand")
	private Decision errandDecision;

}
