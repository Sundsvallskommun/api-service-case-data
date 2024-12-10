package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
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
public class Law {

	@Schema(description = "Heading of the law", example = "Building Act")
	@Size(max = 255)
	private String heading;

	@Schema(description = "Swedish Code of Statutes (SFS)", example = "SFS 2010:900")
	@Size(max = 255)
	private String sfs;

	@Schema(description = "Chapter of the law", example = "3")
	@Size(max = 255)
	private String chapter;

	@Schema(description = "Article of the law", example = "1")
	@Size(max = 255)
	private String article;

}
