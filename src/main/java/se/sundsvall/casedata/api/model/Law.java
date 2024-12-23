package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Law {

	@Schema(description = "Heading of the law", example = "Building Act", maxLength = 255)
	@Size(max = 255)
	private String heading;

	@Schema(description = "Swedish Code of Statutes (SFS)", example = "SFS 2010:900", maxLength = 255)
	@Size(max = 255)
	private String sfs;

	@Schema(description = "Chapter of the law", example = "3", maxLength = 255)
	@Size(max = 255)
	private String chapter;

	@Schema(description = "Article of the law", example = "1", maxLength = 255)
	@Size(max = 255)
	private String article;
}
