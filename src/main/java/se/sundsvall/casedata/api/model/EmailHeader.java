package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import se.sundsvall.casedata.integration.db.model.enums.Header;

@Data
@Builder(setterPrefix = "with")
public class EmailHeader {

	@Schema(description = "An email header", example = "MESSAGE_ID")
	private Header header;

	@Schema(description = "The value of the email header", example = "[\"<this-is-a-test@domain.com>\"]")
	private List<String> values;
}
