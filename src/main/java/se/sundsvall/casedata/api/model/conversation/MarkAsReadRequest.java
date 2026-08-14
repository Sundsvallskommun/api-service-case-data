package se.sundsvall.casedata.api.model.conversation;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.dept44.common.validators.annotation.ValidUuid;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Request to mark messages as read")
public class MarkAsReadRequest {

	@NotEmpty
	@ArraySchema(schema = @Schema(description = "Message IDs to mark as read", example = "d82bd8ac-1507-4d9a-958d-369261eecc15"))
	private List<@ValidUuid String> messageIds;

}
