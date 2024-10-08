package se.sundsvall.casedata.api.model;

import jakarta.validation.constraints.Size;

import se.sundsvall.casedata.integration.db.model.enums.ContactType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@EqualsAndHashCode
public class ContactInformation {

	@Schema(description = "The type of contact information", example = "EMAIL")
	private ContactType contactType;

	@Size(max = 255)
	@Schema(description = "The value of the contact information", example = "someEmail@sundsvall.se@")
	private String value;

}
