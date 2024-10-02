package se.sundsvall.casedata.integration.db.model.enums;

import lombok.Getter;

@Getter
public enum AddressCategory {
	POSTAL_ADDRESS("Postadress"), INVOICE_ADDRESS("Fakturaadress"), VISITING_ADDRESS("Besöksadress");

	private final String text;

	AddressCategory(final String text) {
		this.text = text;
	}
}
