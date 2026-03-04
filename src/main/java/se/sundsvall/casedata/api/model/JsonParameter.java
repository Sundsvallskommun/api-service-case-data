package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.JsonNode;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class JsonParameter {

	@Schema(description = "Parameter key", examples = "formData1")
	@NotBlank
	private String key;

	@Schema(description = "JSON structure value")
	@NotNull
	private JsonNode value;

	@Schema(description = "ID referencing a schema in the json-schema service", examples = "2281_person_1.0")
	@NotBlank
	private String schemaId;
}
