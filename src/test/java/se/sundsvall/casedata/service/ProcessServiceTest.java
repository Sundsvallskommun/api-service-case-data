package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.casedata.api.model.validation.enums.CaseType;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.landandexploitation.LandAndExploitationIntegration;
import se.sundsvall.casedata.integration.parkingpermit.ParkingPermitIntegration;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

	@Mock
	private LandAndExploitationIntegration landAndExploitationIntegrationMock;

	@Mock
	private ParkingPermitIntegration parkingPermitIntegrationMock;

	@Mock
	private ErrandRepository errandRepositoryMock;

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
		final var processId = UUID.randomUUID().toString();
		final var response = new StartProcessResponse().processId(processId);
		when(parkingPermitIntegrationMock.startProcess(errand)).thenReturn(response);

		// Act
		final var result = processService.startProcess(errand);

		// Assert
		assertThat(result).isEqualTo(processId);
		verify(parkingPermitIntegrationMock).startProcess(errand);
		verifyNoMoreInteractions(parkingPermitIntegrationMock);
		verifyNoInteractions(landAndExploitationIntegrationMock);
	}

	@ParameterizedTest
	@MethodSource("mexTypesProvider")
	void startProcess_whenMEX(final String enumValue) {
		// Arrange
		final var errand = createErrandEntity();
		final var processId = UUID.randomUUID().toString();
		final var response = new generated.se.sundsvall.mex.StartProcessResponse().processId(processId);
		errand.setCaseType(enumValue);
		when(landAndExploitationIntegrationMock.startProcess(errand)).thenReturn(response);

		// Act
		final var result = processService.startProcess(errand);

		// Assert
		assertThat(result).isEqualTo(processId);
		verify(landAndExploitationIntegrationMock).startProcess(errand);
		verifyNoMoreInteractions(landAndExploitationIntegrationMock);
		verifyNoInteractions(parkingPermitIntegrationMock);
	}

	@Test
	void updateProcess() {
		// Arrange
		final var errandId = 123L;
		final var errand = createErrandEntity();

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		// Act
		processService.updateProcess(errandId);

		// Assert
		verify(errandRepositoryMock).findById(errandId);
		verify(parkingPermitIntegrationMock).updateProcess(errand);
		verifyNoMoreInteractions(parkingPermitIntegrationMock, errandRepositoryMock);
		verifyNoInteractions(landAndExploitationIntegrationMock);
	}

	@ParameterizedTest
	@MethodSource("mexTypesProvider")
	void updateProcess_whenMEX(final String enumValue) {
		// Arrange
		final var errandId = 123L;
		final var errand = createErrandEntity();
		errand.setCaseType(enumValue);

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		// Act
		processService.updateProcess(errandId);

		// Assert
		verify(landAndExploitationIntegrationMock).updateProcess(errand);
		verifyNoMoreInteractions(landAndExploitationIntegrationMock, errandRepositoryMock);
		verifyNoInteractions(parkingPermitIntegrationMock);
	}

	@Test
	void updateProcess_CamundaUser() {
		// Arrange
		final var errandId = 123L;
		final var errand = createErrandEntity();
		errand.setUpdatedByClient("WSO2_Camunda");

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		// Act
		processService.updateProcess(errandId);

		// Assert
		verify(errandRepositoryMock).findById(errandId);
		verifyNoInteractions(parkingPermitIntegrationMock, landAndExploitationIntegrationMock);
	}

}
