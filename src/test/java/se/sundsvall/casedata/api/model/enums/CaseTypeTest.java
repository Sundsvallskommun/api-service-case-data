package se.sundsvall.casedata.api.model.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.APPEAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.LOST_PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_APPLICATION_FOR_ROAD_ALLOWANCE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_BUILDING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_BUY_LAND_FROM_THE_MUNICIPALITY;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_BUY_SMALL_HOUSE_PLOT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_EARLY_DIALOG_PLAN_NOTIFICATION;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_EASEMENT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_FORWARDED_FROM_CONTACTSUNDSVALL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_HUNTING_LEASE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_INVASIVE_SPECIES;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_INVOICE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LAND_INSTRUCTION;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LAND_RIGHT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LAND_SURVEYING_OFFICE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LEASE_REQUEST;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_LITTERING;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_OTHER;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_PROTECTIVE_HUNTING;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_PUBLIC_SPACE_LEASE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOGUE_PLANNING_NOTICE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_REFERRAL_CONSULTATION;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_REQUEST_FOR_PUBLIC_DOCUMENT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_RETURNED_TO_CONTACT_SUNDSVALL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_ROAD_ASSOCIATION;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_SELL_LAND_TO_THE_MUNICIPALITY;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_SMALL_BOAT_HARBOR_DOCK_PORT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_SQUARE_PLACE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_STORMWATER;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_TERMINATION_OF_LEASE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_TREES_FORESTS;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.MEX_UNAUTHORIZED_RESIDENCE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_BUS_CARD;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_CHANGE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NATIONAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NATIONAL_RENEWAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NOTIFICATION;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NOTIFICATION_BUS_CARD;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NOTIFICATION_CHANGE;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NOTIFICATION_NATIONAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NOTIFICATION_RENEWAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_NOTIFICATION_RIAK;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_RENEWAL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARATRANSIT_RIAK;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;

import org.junit.jupiter.api.Test;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;

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
			MEX_SQUARE_PLACE,
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
			MEX_HUNTING_LEASE,
			MEX_FORWARDED_FROM_CONTACTSUNDSVALL,
			MEX_BUILDING_PERMIT,
			MEX_STORMWATER,
			MEX_INVASIVE_SPECIES,
			MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL,
			MEX_LITTERING,
			MEX_REFERRAL_CONSULTATION,
			MEX_PUBLIC_SPACE_LEASE,
			MEX_EASEMENT,
			MEX_TREES_FORESTS,
			MEX_ROAD_ASSOCIATION,
			MEX_RETURNED_TO_CONTACT_SUNDSVALL,
			MEX_SMALL_BOAT_HARBOR_DOCK_PORT,
			MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE,
			MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS,
			APPEAL,
			PARATRANSIT,
			PARATRANSIT_RENEWAL,
			PARATRANSIT_CHANGE,
			PARATRANSIT_NATIONAL,
			PARATRANSIT_NATIONAL_RENEWAL,
			PARATRANSIT_RIAK,
			PARATRANSIT_BUS_CARD,
			PARATRANSIT_NOTIFICATION,
			PARATRANSIT_NOTIFICATION_CHANGE,
			PARATRANSIT_NOTIFICATION_RENEWAL,
			PARATRANSIT_NOTIFICATION_NATIONAL,
			PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL,
			PARATRANSIT_NOTIFICATION_RIAK,
			PARATRANSIT_NOTIFICATION_BUS_CARD);
	}

	@Test
	void enumToStringParkingPermit() {
		assertThat(PARKING_PERMIT).hasToString("PARKING_PERMIT");
		assertThat(PARKING_PERMIT_RENEWAL).hasToString("PARKING_PERMIT_RENEWAL");
		assertThat(LOST_PARKING_PERMIT).hasToString("LOST_PARKING_PERMIT");
		assertThat(APPEAL).hasToString("APPEAL");
		assertThat(PARATRANSIT).hasToString("PARATRANSIT");
		assertThat(PARATRANSIT_RENEWAL).hasToString("PARATRANSIT_RENEWAL");
		assertThat(PARATRANSIT_CHANGE).hasToString("PARATRANSIT_CHANGE");
		assertThat(PARATRANSIT_NATIONAL).hasToString("PARATRANSIT_NATIONAL");
		assertThat(PARATRANSIT_NATIONAL_RENEWAL).hasToString("PARATRANSIT_NATIONAL_RENEWAL");
		assertThat(PARATRANSIT_RIAK).hasToString("PARATRANSIT_RIAK");
		assertThat(PARATRANSIT_BUS_CARD).hasToString("PARATRANSIT_BUS_CARD");
		assertThat(PARATRANSIT_NOTIFICATION).hasToString("PARATRANSIT_NOTIFICATION");
		assertThat(PARATRANSIT_NOTIFICATION_CHANGE).hasToString("PARATRANSIT_NOTIFICATION_CHANGE");
		assertThat(PARATRANSIT_NOTIFICATION_RENEWAL).hasToString("PARATRANSIT_NOTIFICATION_RENEWAL");
		assertThat(PARATRANSIT_NOTIFICATION_NATIONAL).hasToString("PARATRANSIT_NOTIFICATION_NATIONAL");
		assertThat(PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL).hasToString("PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL");
		assertThat(PARATRANSIT_NOTIFICATION_RIAK).hasToString("PARATRANSIT_NOTIFICATION_RIAK");
		assertThat(PARATRANSIT_NOTIFICATION_BUS_CARD).hasToString("PARATRANSIT_NOTIFICATION_BUS_CARD");
	}

	@Test
	void enumToStringMex() {
		assertThat(MEX_LEASE_REQUEST).hasToString("MEX_LEASE_REQUEST");
		assertThat(MEX_BUY_LAND_FROM_THE_MUNICIPALITY).hasToString("MEX_BUY_LAND_FROM_THE_MUNICIPALITY");
		assertThat(MEX_SELL_LAND_TO_THE_MUNICIPALITY).hasToString("MEX_SELL_LAND_TO_THE_MUNICIPALITY");
		assertThat(MEX_SQUARE_PLACE).hasToString("MEX_SQUARE_PLACE");
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
		assertThat(MEX_HUNTING_LEASE).hasToString("MEX_HUNTING_LEASE");
		assertThat(MEX_FORWARDED_FROM_CONTACTSUNDSVALL).hasToString("MEX_FORWARDED_FROM_CONTACTSUNDSVALL");
		assertThat(MEX_BUILDING_PERMIT).hasToString("MEX_BUILDING_PERMIT");
		assertThat(MEX_STORMWATER).hasToString("MEX_STORMWATER");
		assertThat(MEX_INVASIVE_SPECIES).hasToString("MEX_INVASIVE_SPECIES");
		assertThat(MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL).hasToString("MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL");
		assertThat(MEX_LITTERING).hasToString("MEX_LITTERING");
		assertThat(MEX_REFERRAL_CONSULTATION).hasToString("MEX_REFERRAL_CONSULTATION");
		assertThat(MEX_PUBLIC_SPACE_LEASE).hasToString("MEX_PUBLIC_SPACE_LEASE");
		assertThat(MEX_EASEMENT).hasToString("MEX_EASEMENT");
		assertThat(MEX_TREES_FORESTS).hasToString("MEX_TREES_FORESTS");
		assertThat(MEX_ROAD_ASSOCIATION).hasToString("MEX_ROAD_ASSOCIATION");
		assertThat(MEX_RETURNED_TO_CONTACT_SUNDSVALL).hasToString("MEX_RETURNED_TO_CONTACT_SUNDSVALL");
		assertThat(MEX_SMALL_BOAT_HARBOR_DOCK_PORT).hasToString("MEX_SMALL_BOAT_HARBOR_DOCK_PORT");
		assertThat(MEX_SELL_LAND_TO_THE_MUNICIPALITY).hasToString("MEX_SELL_LAND_TO_THE_MUNICIPALITY");
		assertThat(MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS).hasToString("MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS");
	}

	@Test
	void enumToStringAppeal() {
		assertThat(APPEAL).hasToString("APPEAL");
	}

	@Test
	void getParkingPermitCaseTypes() {
		assertThat(CaseType.getParkingPermitCaseTypes()).containsExactlyInAnyOrder(
			PARKING_PERMIT,
			PARKING_PERMIT_RENEWAL,
			LOST_PARKING_PERMIT,
			APPEAL,
			PARATRANSIT,
			PARATRANSIT_RENEWAL,
			PARATRANSIT_CHANGE,
			PARATRANSIT_NATIONAL,
			PARATRANSIT_NATIONAL_RENEWAL,
			PARATRANSIT_RIAK,
			PARATRANSIT_BUS_CARD,
			PARATRANSIT_NOTIFICATION,
			PARATRANSIT_NOTIFICATION_CHANGE,
			PARATRANSIT_NOTIFICATION_RENEWAL,
			PARATRANSIT_NOTIFICATION_NATIONAL,
			PARATRANSIT_NOTIFICATION_NATIONAL_RENEWAL,
			PARATRANSIT_NOTIFICATION_RIAK,
			PARATRANSIT_NOTIFICATION_BUS_CARD);
	}

	@Test
	void getMexCaseTypes() {
		assertThat(CaseType.getMexCaseTypes()).containsExactlyInAnyOrder(
			MEX_LEASE_REQUEST,
			MEX_BUY_LAND_FROM_THE_MUNICIPALITY,
			MEX_SELL_LAND_TO_THE_MUNICIPALITY,
			MEX_SQUARE_PLACE,
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
			MEX_HUNTING_LEASE,
			MEX_FORWARDED_FROM_CONTACTSUNDSVALL,
			MEX_BUILDING_PERMIT,
			MEX_STORMWATER,
			MEX_INVASIVE_SPECIES,
			MEX_LAND_USE_AGREEMENT_VALUATION_PROTOCOL,
			MEX_LITTERING,
			MEX_REFERRAL_CONSULTATION,
			MEX_PUBLIC_SPACE_LEASE,
			MEX_EASEMENT,
			MEX_TREES_FORESTS,
			MEX_ROAD_ASSOCIATION,
			MEX_RETURNED_TO_CONTACT_SUNDSVALL,
			MEX_SMALL_BOAT_HARBOR_DOCK_PORT,
			MEX_SELL_LAND_TO_THE_MUNICIPALITY_PRIVATE,
			MEX_SELL_LAND_TO_THE_MUNICIPALITY_BUSINESS);
	}
}
