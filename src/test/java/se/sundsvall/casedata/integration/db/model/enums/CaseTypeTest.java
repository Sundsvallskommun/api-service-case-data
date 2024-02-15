package se.sundsvall.casedata.integration.db.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANMALAN_ANDRING_AVLOPPSANLAGGNING;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANMALAN_ANDRING_AVLOPPSANORDNING;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANMALAN_ATTEFALL;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANMALAN_HALSOSKYDDSVERKSAMHET;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANMALAN_INSTALLATION_ENSKILT_AVLOPP_UTAN_WC;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANMALAN_INSTALLATION_VARMEPUMP;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.ANSOKAN_TILLSTAND_VARMEPUMP;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_TERMINATION_OF_HUNTING_LEASE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_TERMINATION_OF_LEASE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.NYBYGGNAD_ANSOKAN_OM_BYGGLOV;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.REGISTRERING_AV_LIVSMEDEL;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.LOST_PARKING_PERMIT;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_LEASE_REQUEST;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_BUY_LAND_FROM_THE_MUNICIPALITY;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_SELL_LAND_TO_THE_MUNICIPALITY;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_APPLICATION_SQUARE_PLACE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_BUY_SMALL_HOUSE_PLOT;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_APPLICATION_FOR_ROAD_ALLOWANCE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_UNAUTHORIZED_RESIDENCE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_LAND_RIGHT;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_EARLY_DIALOG_PLAN_NOTIFICATION;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_PROTECTIVE_HUNTING;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_LAND_INSTRUCTION;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_OTHER;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_LAND_SURVEYING_OFFICE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOUGE_PLANNING_NOTICE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_INVOICE;
import static se.sundsvall.casedata.integration.db.model.enums.CaseType.MEX_REQUEST_FOR_PUBLIC_DOCUMENT;

class CaseTypeTest {

	@Test
	void enumValues() {
		assertThat(CaseType.values()).containsExactlyInAnyOrder(
			ANMALAN_ATTEFALL,
			ANMALAN_ANDRING_AVLOPPSANLAGGNING,
			ANMALAN_ANDRING_AVLOPPSANORDNING,
			ANMALAN_HALSOSKYDDSVERKSAMHET,
			ANMALAN_INSTALLATION_ENSKILT_AVLOPP_UTAN_WC,
			ANMALAN_INSTALLATION_VARMEPUMP,
			ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP,
			ANSOKAN_TILLSTAND_VARMEPUMP,
			NYBYGGNAD_ANSOKAN_OM_BYGGLOV,
			REGISTRERING_AV_LIVSMEDEL,
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
			MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOUGE_PLANNING_NOTICE,
			MEX_INVOICE,
			MEX_REQUEST_FOR_PUBLIC_DOCUMENT,
			MEX_TERMINATION_OF_LEASE,
			MEX_TERMINATION_OF_HUNTING_LEASE);
	}

	@Test
	void enumToStringByggR() {
		assertThat(ANMALAN_ATTEFALL).hasToString("ANMALAN_ATTEFALL");
		assertThat(NYBYGGNAD_ANSOKAN_OM_BYGGLOV).hasToString("NYBYGGNAD_ANSOKAN_OM_BYGGLOV");
	}
	@Test
	void enumToStringEcos() {
		assertThat(ANMALAN_ANDRING_AVLOPPSANLAGGNING).hasToString("ANMALAN_ANDRING_AVLOPPSANLAGGNING");
		assertThat(ANMALAN_ANDRING_AVLOPPSANORDNING).hasToString("ANMALAN_ANDRING_AVLOPPSANORDNING");
		assertThat(ANMALAN_HALSOSKYDDSVERKSAMHET).hasToString("ANMALAN_HALSOSKYDDSVERKSAMHET");
		assertThat(ANMALAN_INSTALLATION_ENSKILT_AVLOPP_UTAN_WC).hasToString("ANMALAN_INSTALLATION_ENSKILT_AVLOPP_UTAN_WC");
		assertThat(ANMALAN_INSTALLATION_VARMEPUMP).hasToString("ANMALAN_INSTALLATION_VARMEPUMP");
		assertThat(ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP).hasToString("ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP");
		assertThat(ANSOKAN_TILLSTAND_VARMEPUMP).hasToString("ANSOKAN_TILLSTAND_VARMEPUMP");
		assertThat(REGISTRERING_AV_LIVSMEDEL).hasToString("REGISTRERING_AV_LIVSMEDEL");
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
		assertThat(MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOUGE_PLANNING_NOTICE).hasToString("MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOUGE_PLANNING_NOTICE");
		assertThat(MEX_INVOICE).hasToString("MEX_INVOICE");
		assertThat(MEX_REQUEST_FOR_PUBLIC_DOCUMENT).hasToString("MEX_REQUEST_FOR_PUBLIC_DOCUMENT");
		assertThat(MEX_TERMINATION_OF_LEASE).hasToString("MEX_TERMINATION_OF_LEASE");
		assertThat(MEX_TERMINATION_OF_HUNTING_LEASE).hasToString("MEX_TERMINATION_OF_HUNTING_LEASE");
	}


	@Test
	void getValuesByAbbreviation() {
		assertThat(CaseType.getValuesByAbbreviation("BUILD")).containsExactlyInAnyOrder(
			NYBYGGNAD_ANSOKAN_OM_BYGGLOV,
			ANMALAN_ATTEFALL);
		assertThat(CaseType.getValuesByAbbreviation("ENV")).containsExactlyInAnyOrder(
			REGISTRERING_AV_LIVSMEDEL,
			ANMALAN_INSTALLATION_VARMEPUMP,
			ANSOKAN_TILLSTAND_VARMEPUMP,
			ANSOKAN_OM_TILLSTAND_ENSKILT_AVLOPP,
			ANMALAN_INSTALLATION_ENSKILT_AVLOPP_UTAN_WC,
			ANMALAN_ANDRING_AVLOPPSANLAGGNING,
			ANMALAN_ANDRING_AVLOPPSANORDNING,
			ANMALAN_HALSOSKYDDSVERKSAMHET);
		assertThat(CaseType.getValuesByAbbreviation("PRH")).containsExactlyInAnyOrder(
			PARKING_PERMIT,
			PARKING_PERMIT_RENEWAL,
			LOST_PARKING_PERMIT);
		assertThat(CaseType.getValuesByAbbreviation("MEX")).containsExactlyInAnyOrder(
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
			MEX_REFERRAL_BUILDING_PERMIT_EARLY_DIALOUGE_PLANNING_NOTICE,
			MEX_INVOICE,
			MEX_REQUEST_FOR_PUBLIC_DOCUMENT,
			MEX_TERMINATION_OF_LEASE,
			MEX_TERMINATION_OF_HUNTING_LEASE);
	}

	@Test
	void getValuesByAbbreviation_empty() {
		assertThat(CaseType.getValuesByAbbreviation("")).isEmpty();
	}

	@Test
	void getValuesByAbbreviation_null() {
		assertThat(CaseType.getValuesByAbbreviation(null)).isEmpty();
	}

	@Test
	void getValuesByAbbreviation_unknown() {
		assertThat(CaseType.getValuesByAbbreviation("UNKNOWN")).isEmpty();
	}
}
