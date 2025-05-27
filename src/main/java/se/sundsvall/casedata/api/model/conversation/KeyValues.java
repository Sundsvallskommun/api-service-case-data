package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "KeyValues model")
public class KeyValues {

	@Schema(description = "The key", example = "key1")
	private String key;

	@ArraySchema(schema = @Schema(implementation = String.class))
	private List<String> values;

}
