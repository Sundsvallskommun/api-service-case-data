package se.sundsvall.casedata.integration.paratransit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.SERVICE_UNAVAILABLE;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;

import generated.se.sundsvall.paratransit.StartProcessResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.AbstractThrowableProblem;
import org.zalando.problem.Problem;

@ExtendWith(MockitoExtension.class)
class ParatransitIntegrationTest {

	@Mock
	private ParatransitClient paratransitClient;

	@InjectMocks
	private ParatransitIntegration paratransitIntegration;

	@Test
	void startProcessTest() {
		final var errand = createErrandEntity();
		final var response = new StartProcessResponse();
		when(paratransitClient.startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId())).thenReturn(response);

		final var result = paratransitIntegration.startProcess(errand);

		assertThat(result).isEqualTo(response);
		verify(paratransitClient).startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId());
	}

	@Test
	void startProcessWhenThrowsTest() {
		final var errand = createErrandEntity();
		when(paratransitClient.startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId())).thenThrow(new AbstractThrowableProblem() {

			private static final long serialVersionUID = 1L;
		});

		assertThatThrownBy(() -> paratransitIntegration.startProcess(errand))
			.isInstanceOf(Problem.class)
			.hasMessage("Service Unavailable: Unexpected response from ProcessEngine API.")
			.hasFieldOrPropertyWithValue("status", SERVICE_UNAVAILABLE);

		verify(paratransitClient).startProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getId());
	}

	@Test
	void updateProcessTest() {
		final var errand = createErrandEntity();
		paratransitIntegration.updateProcess(errand);

		verify(paratransitClient).updateProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getProcessId());
	}

	@Test
	void updateProcessWhenThrowsTest() {
		final var errand = createErrandEntity();
		when(paratransitClient.updateProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getProcessId())).thenThrow(new AbstractThrowableProblem() {

			private static final long serialVersionUID = 1L;
		});

		assertThatThrownBy(() -> paratransitIntegration.updateProcess(errand))
			.isInstanceOf(Problem.class)
			.hasMessage("Service Unavailable: Unexpected response from ProcessEngine API.")
			.hasFieldOrPropertyWithValue("status", SERVICE_UNAVAILABLE);

		verify(paratransitClient).updateProcess(errand.getMunicipalityId(), errand.getNamespace(), errand.getProcessId());
	}
}
