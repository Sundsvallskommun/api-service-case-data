package se.sundsvall.casedata.service;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.landandexploitation.LandAndExploitationIntegration;
import se.sundsvall.casedata.integration.parkingpermit.ParkingPermitIntegration;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

	@Mock
	private LandAndExploitationIntegration landAndExploitationIntegration;

	@Mock
	private ParkingPermitIntegration parkingPermitIntegration;

	@InjectMocks
	private ProcessService processService;

	private static Stream<Arguments> mexTypesProvider() {
		return CaseType.getMexCaseTypes().stream()
			.map(Object::toString)
			.map(Arguments::of);
	}

	private static Stream<Arguments> parkingPermitTypesProvider() {
		return CaseType.getParkingPermitCaseTypes().stream()
			.map(Object::toString)
			.map(Arguments::of);
	}

	@ParameterizedTest
	@MethodSource("parkingPermitTypesProvider")
	void startProcess_whenParkingPermit(final String enumValue) {
		// Arrange
		final var errand = createErrandEntity();
		final var result = new StartProcessResponse();
		errand.setCaseType(enumValue);
		when(parkingPermitIntegration.startProcess(errand)).thenReturn(result);

		// Act
		final var result1 = processService.startProcess(errand);

		// Assert
		assertThat(result1).isEqualTo(result);
		verify(parkingPermitIntegration).startProcess(errand);
		verifyNoMoreInteractions(parkingPermitIntegration);
		verifyNoInteractions(landAndExploitationIntegration);
	}

	@ParameterizedTest
	@MethodSource("mexTypesProvider")
	void startProcess_whenMEX(final String enumValue) {
		// Arrange
		final var errand = createErrandEntity();
		final var result = new StartProcessResponse();
		errand.setCaseType(enumValue);
		when(landAndExploitationIntegration.startProcess(errand)).thenReturn(result);

		// Act
		final var result1 = processService.startProcess(errand);

		// Assert
		assertThat(result1).isEqualTo(result);
		verify(landAndExploitationIntegration).startProcess(errand);
		verifyNoMoreInteractions(landAndExploitationIntegration);
		verifyNoInteractions(parkingPermitIntegration);
	}

	@ParameterizedTest
	@MethodSource("parkingPermitTypesProvider")
	void updateProcess_whenParkingPermit(final String enumValue) {
		// Arrange
		final var errand = createErrandEntity();
		errand.setCaseType(enumValue);

		// Act
		processService.updateProcess(errand);

		// Assert
		verify(parkingPermitIntegration).updateProcess(errand);
		verifyNoMoreInteractions(parkingPermitIntegration);
		verifyNoInteractions(landAndExploitationIntegration);
	}

	@ParameterizedTest
	@MethodSource("mexTypesProvider")
	void updateProcess_whenMEX(final String enumValue) {
		// Arrange
		final var errand = createErrandEntity();
		errand.setCaseType(enumValue);

		// Act
		processService.updateProcess(errand);

		// Assert
		verify(landAndExploitationIntegration).updateProcess(errand);
		verifyNoMoreInteractions(landAndExploitationIntegration);
		verifyNoInteractions(parkingPermitIntegration);
	}

	@Test
	void updateProcess_CamundaUser() {
		// Arrange
		final var errand = createErrandEntity();
		errand.setUpdatedByClient("WSO2_Camunda");

		// Act
		processService.updateProcess(errand);

		// Assert
		verifyNoInteractions(parkingPermitIntegration, landAndExploitationIntegration);
	}

}
