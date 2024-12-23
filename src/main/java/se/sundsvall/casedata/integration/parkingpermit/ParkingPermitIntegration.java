package se.sundsvall.casedata.integration.parkingpermit;

import static org.zalando.problem.Status.SERVICE_UNAVAILABLE;
import static se.sundsvall.casedata.service.util.Constants.PROCESS_ENGINE_PROBLEM_DETAIL;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;

@Component
public class ParkingPermitIntegration {

	private static final String COULD_NOT_START_PROCESS = "Could not start process for errand with casetype: %s and errandId: %s, with error: %s";

	private static final String COULD_NOT_UPDATE_PROCESS = "Could not update process for errand with casetype: %s and processId: %s, with error: %s";

	private static final Logger LOGGER = LoggerFactory.getLogger(ParkingPermitIntegration.class);

	private final ParkingPermitClient parkingPermitClient;

	public ParkingPermitIntegration(final ParkingPermitClient parkingPermitClient) {
		this.parkingPermitClient = parkingPermitClient;
	}

	public StartProcessResponse startProcess(final ErrandEntity errand) {
		try {
			return parkingPermitClient.startProcess(errand.getMunicipalityId(), errand.getId());
		} catch (final AbstractThrowableProblem e) {
			LOGGER.warn(COULD_NOT_START_PROCESS.formatted(errand.getCaseType(), errand.getId(), e));
			throw Problem.valueOf(SERVICE_UNAVAILABLE, PROCESS_ENGINE_PROBLEM_DETAIL);
		}
	}

	public void updateProcess(final ErrandEntity errand) {
		try {
			parkingPermitClient.updateProcess(errand.getMunicipalityId(), errand.getProcessId());
		} catch (final AbstractThrowableProblem e) {
			LOGGER.warn(COULD_NOT_UPDATE_PROCESS.formatted(errand.getCaseType(), errand.getProcessId(), e));
			throw Problem.valueOf(SERVICE_UNAVAILABLE, PROCESS_ENGINE_PROBLEM_DETAIL);
		}
	}

}
