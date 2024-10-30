package se.sundsvall.casedata.api.model.enums;

import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.APPEAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.LOST_PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_APPLICATION_FOR_ROAD_ALLOWANCE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_APPLICATION_SQUARE_PLACE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_BUY_LAND_FROM_THE_MUNICIPALITY;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_BUY_SMALL_HOUSE_PLOT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_EARLY_DIALOG_PLAN_NOTIFICATION;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_FORWARDED_FROM_CONTACTSUNDSVALL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_INVOICE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LAND_INSTRUCTION;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LAND_RIGHT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LAND_SURVEYING_OFFICE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LEASE_REQUEST;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_OTHER;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_PROTECTIVE_HUNTING;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_REQUEST_FOR_PUBLIC_DOCUMENT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_SELL_LAND_TO_THE_MUNICIPALITY;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_TERMINATION_OF_HUNTING_LEASE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_TERMINATION_OF_LEASE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_UNAUTHORIZED_RESIDENCE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;

class CaseTypeTest {

	@Test
	void enumValues() {
		assertThat(CaseType.values()).containsExactlyInAnyOrder(
			PARKING_PERMIT,
			PARKING_PERMIT_RENEWAL,
			LOST_PARKING_PERMIT,
			MEX_LEASE_REQUEST,
			MEX_BUY_LAND_FROM_THE_MUNICIPALITY,
			MEX_SELL_LAND_TO_THE_MUNICIPALITY,
			MEX_APPLICATION_SQUARE_PLACE,
			MEX_BUY_SMALL_HOUSE_PLOT,
			MEX_APPLICATION_FOR_ROAD_ALLOWANCE,
			MEX_UNAUTHORIZED_RESIDENCE,
			MEX_LAND_RIGHT,
			MEX_EARLY_DIALOG_PLAN_NOTIFICATION,
			MEX_PROTECTIVE_HUNTING,
			MEX_LAND_INSTRUCTION,
			MEX_OTHER,
			MEX_LAND_SURVEYING_OFFICE,
			MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE,
			MEX_INVOICE,
			MEX_REQUEST_FOR_PUBLIC_DOCUMENT,
			MEX_TERMINATION_OF_LEASE,
			MEX_TERMINATION_OF_HUNTING_LEASE,
			MEX_FORWARDED_FROM_CONTACTSUNDSVALL,
			APPEAL);
	}

	@Test
	void enumToStringParkingPermit() {
		assertThat(PARKING_PERMIT).hasToString("PARKING_PERMIT");
		assertThat(PARKING_PERMIT_RENEWAL).hasToString("PARKING_PERMIT_RENEWAL");
		assertThat(LOST_PARKING_PERMIT).hasToString("LOST_PARKING_PERMIT");
	}

	@Test
	void enumToStringMex() {
		assertThat(MEX_LEASE_REQUEST).hasToString("MEX_LEASE_REQUEST");
		assertThat(MEX_BUY_LAND_FROM_THE_MUNICIPALITY).hasToString("MEX_BUY_LAND_FROM_THE_MUNICIPALITY");
		assertThat(MEX_SELL_LAND_TO_THE_MUNICIPALITY).hasToString("MEX_SELL_LAND_TO_THE_MUNICIPALITY");
		assertThat(MEX_APPLICATION_SQUARE_PLACE).hasToString("MEX_APPLICATION_SQUARE_PLACE");
		assertThat(MEX_BUY_SMALL_HOUSE_PLOT).hasToString("MEX_BUY_SMALL_HOUSE_PLOT");
		assertThat(MEX_APPLICATION_FOR_ROAD_ALLOWANCE).hasToString("MEX_APPLICATION_FOR_ROAD_ALLOWANCE");
		assertThat(MEX_UNAUTHORIZED_RESIDENCE).hasToString("MEX_UNAUTHORIZED_RESIDENCE");
		assertThat(MEX_LAND_RIGHT).hasToString("MEX_LAND_RIGHT");
		assertThat(MEX_EARLY_DIALOG_PLAN_NOTIFICATION).hasToString("MEX_EARLY_DIALOG_PLAN_NOTIFICATION");
		assertThat(MEX_PROTECTIVE_HUNTING).hasToString("MEX_PROTECTIVE_HUNTING");
		assertThat(MEX_LAND_INSTRUCTION).hasToString("MEX_LAND_INSTRUCTION");
		assertThat(MEX_OTHER).hasToString("MEX_OTHER");
		assertThat(MEX_LAND_SURVEYING_OFFICE).hasToString("MEX_LAND_SURVEYING_OFFICE");
		assertThat(MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE).hasToString("MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE");
		assertThat(MEX_INVOICE).hasToString("MEX_INVOICE");
		assertThat(MEX_REQUEST_FOR_PUBLIC_DOCUMENT).hasToString("MEX_REQUEST_FOR_PUBLIC_DOCUMENT");
		assertThat(MEX_TERMINATION_OF_LEASE).hasToString("MEX_TERMINATION_OF_LEASE");
		assertThat(MEX_TERMINATION_OF_HUNTING_LEASE).hasToString("MEX_TERMINATION_OF_HUNTING_LEASE");
		assertThat(MEX_FORWARDED_FROM_CONTACTSUNDSVALL).hasToString("MEX_FORWARDED_FROM_CONTACTSUNDSVALL");
	}

	@Test
	void enumToStringAppeal() {
		assertThat(APPEAL).hasToString("APPEAL");
	}

}
