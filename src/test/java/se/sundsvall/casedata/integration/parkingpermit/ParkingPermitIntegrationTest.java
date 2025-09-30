package se.sundsvall.casedata.integration.parkingpermit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.SERVICE_UNAVAILABLE;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class ParkingPermitIntegrationTest {

	@Mock
	private ParkingPermitClient parkingPermitClient;

	@InjectMocks
	private ParkingPermitIntegration parkingPermitIntegration;

	@Test
	void startProcessTest() {
		final var errand = createErrandEntity();
		final var response = new StartProcessResponse();
		when(parkingPermitClient.startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId())).thenReturn(response);

		final var result = parkingPermitIntegration.startProcess(errand);

		assertThat(result).isEqualTo(response);
		verify(parkingPermitClient).startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId());
	}

	@Test
	void startProcessWhenThrowsTest() {
		final var errand = createErrandEntity();
		when(parkingPermitClient.startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId())).thenThrow(new AbstractThrowableProblem() {

			private static final long serialVersionUID = 1L;
		});

		assertThatThrownBy(() -> parkingPermitIntegration.startProcess(errand))
			.isInstanceOf(Problem.class)
			.hasMessage("Service Unavailable: Unexpected response from ProcessEngine API.")
			.hasFieldOrPropertyWithValue("status", SERVICE_UNAVAILABLE);

		verify(parkingPermitClient).startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId());
	}

	@Test
	void updateProcessTest() {
		final var errand = createErrandEntity();
		parkingPermitIntegration.updateProcess(errand);

		verify(parkingPermitClient).updateProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getProcessId());
	}

	@Test
	void updateProcessWhenThrowsTest() {
		final var errand = createErrandEntity();
		when(parkingPermitClient.updateProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getProcessId())).thenThrow(new AbstractThrowableProblem() {

			private static final long serialVersionUID = 1L;
		});

		assertThatThrownBy(() -> parkingPermitIntegration.updateProcess(errand))
			.isInstanceOf(Problem.class)
			.hasMessage("Service Unavailable: Unexpected response from ProcessEngine API.")
			.hasFieldOrPropertyWithValue("status", SERVICE_UNAVAILABLE);

		verify(parkingPermitClient).updateProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getProcessId());
	}
}
