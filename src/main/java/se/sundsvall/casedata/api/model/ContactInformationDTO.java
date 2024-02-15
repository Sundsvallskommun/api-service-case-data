package se.sundsvall.casedata.api.model;

import jakarta.validation.constraints.Size;

import se.sundsvall.casedata.integration.db.model.enums.ContactType;

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
public class ContactInformationDTO {

	private ContactType contactType;

	@Size(max = 255)
	private String value;

}
