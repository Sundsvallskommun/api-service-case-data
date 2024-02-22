package se.sundsvall.casedata.service;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.sundsvall.casedata.api.model.enums.CaseType;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.landandexploitation.LandAndExploitationIntegration;
import se.sundsvall.casedata.integration.parkingpermit.ParkingPermitIntegration;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.casedata.service.util.Constants.CAMUNDA_USER;
import static se.sundsvall.casedata.service.util.Constants.MEX_CASE_TYPES;
import static se.sundsvall.casedata.service.util.Constants.PARKING_PERMIT_CASE_TYPES;

@Service
public class ProcessService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);
	private final ParkingPermitIntegration parkingPermitIntegration;
	private final LandAndExploitationIntegration landAndExploitationIntegration;

	public ProcessService(final ParkingPermitIntegration parkingPermitIntegration,
		final LandAndExploitationIntegration landAndExploitationIntegration) {
		this.parkingPermitIntegration = parkingPermitIntegration;
		this.landAndExploitationIntegration = landAndExploitationIntegration;
	}

	public StartProcessResponse startProcess(final Errand errand) {
		if (PARKING_PERMIT_CASE_TYPES.contains(CaseType.valueOf(errand.getCaseType()))) {
			return parkingPermitIntegration.startProcess(errand);
		} else if (MEX_CASE_TYPES.contains(CaseType.valueOf(errand.getCaseType()))) {
			return landAndExploitationIntegration.startProcess(errand);
		}
		LOGGER.info("No camunda process found for caseType: {}", errand.getCaseType());
		return null;
	}

	public void updateProcess(final Errand errand) {
		if (errand.getUpdatedByClient().equals(CAMUNDA_USER)) {
			LOGGER.warn("Errand with id: {} was updated by camunda user, no need to update process", errand.getId());
			return;
		}
		if (isValidParkingPermitCase(errand)) {
			parkingPermitIntegration.updateProcess(errand);
		} else if (isValidMexCase(errand)) {
			landAndExploitationIntegration.updateProcess(errand);
		} else {
			LOGGER.info("No camunda process found for updating case with caseType: {}", errand.getCaseType());
		}
	}

	private boolean isValidParkingPermitCase(final Errand errand) {
		return PARKING_PERMIT_CASE_TYPES.contains(CaseType.valueOf(errand.getCaseType())) && isNotEmpty(errand.getProcessId());
	}

	private boolean isValidMexCase(final Errand errand) {
		return MEX_CASE_TYPES.contains(CaseType.valueOf(errand.getCaseType())) && isNotEmpty(errand.getProcessId());
	}
}
