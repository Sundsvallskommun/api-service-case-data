package se.sundsvall.casedata.api.model.enums;

import lombok.Getter;

@Getter
public enum StakeholderRole {

	// Kontrollansvarig
	CONTROL_OFFICIAL("KOA"),
	// Sökande
	APPLICANT("SOK"),
	// Fastighetsägare
	PROPERTY_OWNER("FAG"),
	// Betalningsansvarig
	PAYMENT_PERSON("BETA"),

	INVOICE_RECIPENT("Fakturamottagare"),
	OPERATOR("Verksamhetsutövare"),

	CONTACT_PERSON("KPER"),
	ADMINISTRATOR("Handläggare"),
	FELLOW_APPLICANT("Medsökande"),
	DRIVER("Förare"),
	PASSENGER("Passagerare"),
	DOCTOR("Läkare"),

	GRANTOR("Upplåtare"),
	SELLER("Säljare"),
	BUYER("Köpare"),
	LEASEHOLDER("Arrendator"),
	COMPANY_SIGNATORY("Firmatecknare"),
	ASSOCIATION_REPRESENTATIVE("Föreningsrepresentant"),
	CASHIER("Kassör"),
	CHAIRMAN("Ordförande");

	private final String text;

	StakeholderRole(final String text) {
		this.text = text;
	}

}
