package se.sundsvall.casedata.api.model.validation.enums;

public enum StakeholderRole {
	// Kontrollansvarig
	CONTROL_OFFICIAL,
	// Sökande
	APPLICANT,
	// Fastighetsägare
	PROPERTY_OWNER,
	// Betalningsansvarig
	PAYMENT_PERSON,
	// "Fakturamottagare"
	INVOICE_RECIPIENT,
	// "Fakturamottagare" Remove when Open-E platform is ready
	@Deprecated(since = "2024-02-27")
	INVOICE_RECIPENT,
	// "Verksamhetsutövare"
	OPERATOR,
	// "KPER"
	CONTACT_PERSON,
	// "Handläggare"
	ADMINISTRATOR,
	// "Medsökande"
	FELLOW_APPLICANT,
	// "Förare"
	DRIVER,
	// "Passagerare"
	PASSENGER,
	// "Läkare"
	DOCTOR,
	// "Upplåtare"
	GRANTOR,
	// "Säljare"
	SELLER,
	// "Köpare"
	BUYER,
	// "Arrendator"
	LEASEHOLDER,
	// "Firmatecknare"
	COMPANY_SIGNATORY,
	// "Föreningsrepresentant"
	ASSOCIATION_REPRESENTATIVE,
	// "Kassör"
	CASHIER,
	// "Ordförande"
	CHAIRMAN,
	// Tomträttshavare
	LAND_RIGHT_OWNER,
	// Ombud
	DELEGATE,
	// Nyttjanderättshavare
	USUFRUCTUARY,
	// Person
	PERSON,
	// Företag
	ORGANIZATION,
	// Rättighetshavare
	RIGHTS_HOLDER

}
