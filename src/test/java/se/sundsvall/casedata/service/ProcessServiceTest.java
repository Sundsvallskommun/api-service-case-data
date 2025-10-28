package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;
import java.util.List;
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
import se.sundsvall.casedata.integration.landandexploitation.LandAndExploitationIntegration;
import se.sundsvall.casedata.integration.landandexploitation.configuration.LandAndExploitationProperties;
import se.sundsvall.casedata.integration.paratransit.ParatransitIntegration;
import se.sundsvall.casedata.integration.parkingpermit.ParkingPermitIntegration;
import se.sundsvall.casedata.integration.parkingpermit.configuration.ParkingPermitProperties;

@ExtendWith(MockitoExtension.class)
class ProcessServiceTest {

	@Mock
	private ParkingPermitProperties parkingPermitPropertiesMock;

	@Mock
	private LandAndExploitationProperties landAndExploitationPropertiesMock;

	@Mock
	private LandAndExploitationIntegration landAndExploitationIntegrationMock;

	@Mock
	private ParkingPermitIntegration parkingPermitIntegrationMock;

	@Mock
	private ParatransitIntegration paratransitIntegrationMock;

	@InjectMocks
	private ProcessService processService;

	private static Stream<Arguments> mexTypesProvider() {
		return Stream.of(
			Arguments.of("MEX_TYPE_1"),
			Arguments.of("MEX_TYPE_2"));
	}

	private static Stream<Arguments> paratransitTypesProvider() {
		return Stream.of(
			Arguments.of("PARATRANSIT"),
			Arguments.of("PARATRANSIT_RENEWAL"));
	}

	private static Stream<Arguments> parkingPermitTypesProvider() {
		return Stream.of(
			Arguments.of("PARKING_PERMIT"),
			Arguments.of("LOST_PARKING_PERMIT"),
			Arguments.of("PARKING_PERMIT_RENEWAL"));
	}

	@ParameterizedTest
	@MethodSource("parkingPermitTypesProvider")
	void startProcessWhenParkingPermit(final String caseType) {
		// Arrange
		final var errand = createErrandEntity();
		errand.setNamespace("SBK_PARKING_PERMIT");
		errand.setCaseType(caseType);
		final var processId = UUID.randomUUID().toString();
		final var responseParkingPermit = new StartProcessResponse().processId(processId);

		when(parkingPermitIntegrationMock.startProcess(errand)).thenReturn(responseParkingPermit);
		when(parkingPermitPropertiesMock.supportedNamespaces()).thenReturn(List.of("SBK_PARKING_PERMIT"));

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
	void startProcessWhenMEX(final String caseType) {
		// Arrange
		final var errand = createErrandEntity();
		errand.setNamespace("SBK_MEX");
		errand.setCaseType(caseType);

		final var processId = UUID.randomUUID().toString();
		final var response = new generated.se.sundsvall.mex.StartProcessResponse().processId(processId);
		when(landAndExploitationIntegrationMock.startProcess(errand)).thenReturn(response);
		when(landAndExploitationPropertiesMock.supportedNamespaces()).thenReturn(List.of("SBK_MEX"));

		// Act
		final var result = processService.startProcess(errand);

		// Assert
		assertThat(result).isEqualTo(processId);
		verify(landAndExploitationIntegrationMock).startProcess(errand);
		verifyNoMoreInteractions(landAndExploitationIntegrationMock);
		verifyNoInteractions(parkingPermitIntegrationMock);
	}

	@ParameterizedTest
	@MethodSource("parkingPermitTypesProvider")
	void updateProcessWhenParkingPermit(final String caseType) {
		// Arrange
		final var errand = createErrandEntity();
		errand.setNamespace("SBK_PARKING_PERMIT");
		errand.setCaseType(caseType);

		when(parkingPermitPropertiesMock.supportedNamespaces()).thenReturn(List.of("SBK_PARKING_PERMIT"));

		// Act
		processService.updateProcess(errand);

		// Assert
		verify(parkingPermitIntegrationMock).updateProcess(errand);
		verifyNoMoreInteractions(parkingPermitIntegrationMock);
		verifyNoInteractions(landAndExploitationIntegrationMock);
	}

	@ParameterizedTest
	@MethodSource("paratransitTypesProvider")
	void updateProcessWhenParatransit(final String caseType) {
		// Arrange
		final var errand = createErrandEntity();
		errand.setNamespace("SBK_PARKING_PERMIT");
		errand.setCaseType(caseType);

		when(parkingPermitPropertiesMock.supportedNamespaces()).thenReturn(List.of("SBK_PARKING_PERMIT"));

		// Act
		processService.updateProcess(errand);

		// Assert
		verify(paratransitIntegrationMock).updateProcess(errand);
		verifyNoMoreInteractions(paratransitIntegrationMock);
		verifyNoInteractions(landAndExploitationIntegrationMock, parkingPermitIntegrationMock);
	}

	@ParameterizedTest
	@MethodSource("mexTypesProvider")
	void updateProcessWhenMEX(final String caseType) {
		// Arrange
		final var errand = createErrandEntity();
		errand.setNamespace("SBK_MEX");
		errand.setCaseType(caseType);
		when(landAndExploitationPropertiesMock.supportedNamespaces()).thenReturn(List.of("SBK_MEX"));

		// Act
		processService.updateProcess(errand);

		// Assert
		verify(landAndExploitationIntegrationMock).updateProcess(errand);
		verifyNoMoreInteractions(landAndExploitationIntegrationMock);
		verifyNoInteractions(parkingPermitIntegrationMock);
	}

	@Test
	void updateProcessCamundaUser() {
		// Arrange
		final var errand = createErrandEntity();
		errand.setUpdatedByClient("WSO2_Camunda");

		// Act
		processService.updateProcess(errand);

		// Assert
		verifyNoInteractions(parkingPermitIntegrationMock, landAndExploitationIntegrationMock);
	}
}
