package se.sundsvall.casedata.api.model;

import java.util.List;

import se.sundsvall.casedata.integration.db.model.enums.Header;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(setterPrefix = "with")
public class EmailHeaderDTO {

	@Schema(description = "An email header", example = "MESSAGE_ID")
	private Header header;

	@Schema(description = "The value of the email header", example = "[<this-is-a-test@domain.com>]")
	private List<String> values;
}
