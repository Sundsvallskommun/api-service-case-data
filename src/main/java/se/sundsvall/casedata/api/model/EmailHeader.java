package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casedata.integration.db.model.enums.Header;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class EmailHeader {

	@Schema(description = "An email header", examples = "MESSAGE_ID")
	private Header header;

	@Schema(description = "The value of the email header", examples = "[\"<this-is-a-test@domain.com>\"]")
	private List<String> values;
}
