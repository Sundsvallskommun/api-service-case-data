package se.sundsvall.casedata.api.model;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
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
