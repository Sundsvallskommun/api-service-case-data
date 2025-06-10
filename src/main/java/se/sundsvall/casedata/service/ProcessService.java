package se.sundsvall.casedata.service;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.service.util.Constants.CAMUNDA_USER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.landandexploitation.LandAndExploitationIntegration;
import se.sundsvall.casedata.integration.parkingpermit.ParkingPermitIntegration;

@Service
public class ProcessService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);

	private final ParkingPermitIntegration parkingPermitIntegration;
	private final LandAndExploitationIntegration landAndExploitationIntegration;
	private final ErrandRepository errandRepository;

	public ProcessService(final ParkingPermitIntegration parkingPermitIntegration,
		final LandAndExploitationIntegration landAndExploitationIntegration, ErrandRepository errandRepository) {
		this.parkingPermitIntegration = parkingPermitIntegration;
		this.landAndExploitationIntegration = landAndExploitationIntegration;
		this.errandRepository = errandRepository;
	}

	public String startProcess(final ErrandEntity errand) {
		if (CaseType.getParkingPermitCaseTypes().contains(CaseType.valueOf(errand.getCaseType()))) {
			return parkingPermitIntegration.startProcess(errand).getProcessId();
		}
		if (CaseType.getMexCaseTypes().contains(CaseType.valueOf(errand.getCaseType()))) {
			return landAndExploitationIntegration.startProcess(errand).getProcessId();
		}
		LOGGER.info("No camunda process found for caseType: {}", errand.getCaseType());
		return null;
	}

	public void updateProcess(final Long errandId) {
		final var errand = errandRepository.findById(errandId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Errand with id %s not found".formatted(errandId)));

		if (CAMUNDA_USER.equals(errand.getUpdatedByClient())) {
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

	private boolean isValidParkingPermitCase(final ErrandEntity errand) {
		return CaseType.getParkingPermitCaseTypes().contains(CaseType.valueOf(errand.getCaseType())) && isNotEmpty(errand.getProcessId());
	}

	private boolean isValidMexCase(final ErrandEntity errand) {
		return CaseType.getMexCaseTypes().contains(CaseType.valueOf(errand.getCaseType())) && isNotEmpty(errand.getProcessId());
	}
}
