package se.sundsvall.casedata.api.model;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class JsonParameter {

	@Schema(description = "Parameter key", examples = "formData1")
	@NotBlank
	private String key;

	@Schema(description = "JSON structure value", examples = """
		{
		  "firstName": "Joe",
		  "lastName": "Doe"
		}
		""")
	@NotNull
	private JsonNode value;

	@Schema(description = "ID referencing a schema in the json-schema service", examples = "2281_person_1.0")
	@NotBlank
	private String schemaId;
}
