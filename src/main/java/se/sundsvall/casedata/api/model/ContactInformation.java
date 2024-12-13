package se.sundsvall.casedata.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class ContactInformation {

	@Schema(description = "The type of contact information", example = "EMAIL")
	private ContactType contactType;

	@Size(max = 255)
	@Schema(description = "The value of the contact information", example = "someEmail@sundsvall.se@")
	private String value;

}
