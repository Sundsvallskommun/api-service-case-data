package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class ExtraParameter {

	@Schema(description = "Parameter key")
	@NotBlank
	private String key;

	@Schema(description = "Parameter display name")
	private String displayName;

	@Schema(description = "Parameter values")
	private List<String> values;

}
