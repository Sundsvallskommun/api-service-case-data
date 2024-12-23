package se.sundsvall.casedata.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;

@Getter
@Setter
@ToString
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class ContactInformationEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "contact_type")
	private ContactType contactType;

	@Column(name = "value")
	private String value;

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof final ContactInformationEntity that)) {
			return false;
		}
		return (contactType == that.contactType) && Objects.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contactType, value);
	}

}
