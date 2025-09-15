package se.sundsvall.casedata.service;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.sundsvall.casedata.service.util.Constants.CAMUNDA_USER;
import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.landandexploitation.LandAndExploitationIntegration;
import se.sundsvall.casedata.integration.landandexploitation.configuration.LandAndExploitationProperties;
import se.sundsvall.casedata.integration.parkingpermit.ParkingPermitIntegration;
import se.sundsvall.casedata.integration.parkingpermit.configuration.ParkingPermitProperties;

@Service
public class ProcessService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessService.class);

	private final ParkingPermitIntegration parkingPermitIntegration;
	private final LandAndExploitationIntegration landAndExploitationIntegration;
	private final ParkingPermitProperties parkingPermitProperties;
	private final LandAndExploitationProperties landAndExploitationProperties;

	public ProcessService(final ParkingPermitIntegration parkingPermitIntegration,
		final LandAndExploitationIntegration landAndExploitationIntegration, final ParkingPermitProperties parkingPermitProperties, final LandAndExploitationProperties landAndExploitationProperties) {
		this.parkingPermitIntegration = parkingPermitIntegration;
		this.landAndExploitationIntegration = landAndExploitationIntegration;
		this.parkingPermitProperties = parkingPermitProperties;
		this.landAndExploitationProperties = landAndExploitationProperties;
	}

	public String startProcess(final ErrandEntity errand) {

		if (parkingPermitProperties.supportedNamespaces().contains(errand.getNamespace())) {
			return parkingPermitIntegration.startProcess(errand).getProcessId();
		}
		if (landAndExploitationProperties.supportedNamespaces().contains(errand.getNamespace())) {
			return landAndExploitationIntegration.startProcess(errand).getProcessId();
		}
		final var sanitizedNamespace = sanitizeForLogging(errand.getNamespace());
		LOGGER.info("No camunda process found for namespace: {}", sanitizedNamespace);
		return null;
	}

	@TransactionalEventListener
	public void updateProcess(final ErrandEntity errand) {

		if (CAMUNDA_USER.equals(errand.getUpdatedByClient())) {
			LOGGER.warn("Errand with id: {} was updated by camunda user, no need to update process", errand.getId());
			return;
		}
		if (isValidParkingPermitCase(errand)) {
			parkingPermitIntegration.updateProcess(errand);
		}
		if (isValidMexCase(errand)) {
			landAndExploitationIntegration.updateProcess(errand);
		} else {
			final var sanitizedNamespace = sanitizeForLogging(errand.getNamespace());
			LOGGER.info("No camunda process found for updating case with namespace: {}", sanitizedNamespace);
		}
	}

	private boolean isValidParkingPermitCase(final ErrandEntity errand) {
		return parkingPermitProperties.supportedNamespaces().contains(errand.getNamespace()) && isNotEmpty(errand.getProcessId());
	}

	private boolean isValidMexCase(final ErrandEntity errand) {
		return landAndExploitationProperties.supportedNamespaces().contains(errand.getNamespace()) && isNotEmpty(errand.getProcessId());
	}

}
