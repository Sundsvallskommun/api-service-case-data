package se.sundsvall.casedata.api.model;

import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder(setterPrefix = "with")
public class LawDTO {

	// Rubrik
	@Size(max = 255)
	private String heading;

	// Svensk författningssamling, (SFS)
	@Size(max = 255)
	private String sfs;

	// kapitel
	@Size(max = 255)
	private String chapter;

	// paragraf
	@Size(max = 255)
	private String article;

}
