package se.sundsvall.casedata.api.model.validation.enums;

import lombok.Getter;

@Getter
public enum AttachmentCategory {

	///////////////////////////////////
	// PARKING PERMIT
	/// ////////////////////////////////
	MEDICAL_CONFIRMATION,
	POLICE_REPORT,
	PASSPORT_PHOTO,
	SIGNATURE,
	POWER_OF_ATTORNEY,
	OTHER,

	/// ////////////////////////////////
	ERRAND_SCANNED_APPLICATION,
	SERVICE_RECEIPT,
	OTHER_ATTACHMENT,

	///////////////////////////////////
	// MEX
	/// ////////////////////////////////
	LEASE_REQUEST,
	RECEIVED_MAP,
	RECEIVED_CONTRACT,
	LAND_PURCHASE_REQUEST,
	INQUIRY_LAND_SALE,
	APPLICATION_SQUARE_PLACE,
	CORPORATE_TAX_CARD,
	TERMINATION_OF_HUNTING_RIGHTS,
	REQUEST_TO_BUY_SMALL_HOUSE_PLOT,
	CONTRACT_DRAFT,

	OEP_APPLICATION, // Ansökan
	ROAD_ALLOWANCE_APPROVAL,// Godkännande för vägbidrag
	MEX_PROTOCOL, // "Protokoll"
	PREVIOUS_AGREEMENT, // Tidigare avtal

	SITUATION_PLAN, // Situationsplan
	EMAIL, // E-post
	LETTER, // Brev

}
