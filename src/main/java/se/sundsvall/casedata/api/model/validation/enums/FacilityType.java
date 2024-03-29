package se.sundsvall.casedata.api.model.validation.enums;

public enum FacilityType {
	// ByggR - Ärendeklasser (Used for CaseType=NYBYGGNAD_ANSOKAN_OM_BYGGLOV)
	ONE_FAMILY_HOUSE,
	APARTMENT_BLOCK,
	WEEKEND_COTTAGE,
	OFFICE_BUILDING,
	INDUSTRIAL_BUILDING,
	GARAGE,
	CARPORT,
	STOREHOUSE,
	GREENHOUSE,
	GUEST_HOUSE,
	WAREHOUSE,
	WORKSHOP_BUILDING,
	RESTAURANT,
	SCHOOL,
	PRESCHOOL,
	// Parkering & Cykelparkering
	PARKING,
	DEPOT,
	MARINA,
	WALL,
	PALING,
	RECYCLING_STATION,
	OTHER,
	// ByggR - Ärendeslag (Used for CaseType=ANMALAN_ATTEFALL)
	FURNISHING_OF_ADDITIONAL_DWELLING,
	ANCILLARY_BUILDING,
	ANCILLARY_HOUSING_BUILDING,
	DORMER,
	EXTENSION
}
