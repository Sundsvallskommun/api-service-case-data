package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.casedata.integration.db.ErrandRepository;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

	@InjectMocks
	private StatusService statusService;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ProcessService processServiceMock;

	@Test
	void replaceStatusesOnErrandTest() {

		// Arrange
		final var errand = createErrandEntity();
		final var statuses = List.of(createStatus(), createStatus(), createStatus());
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		statusService.replaceStatusesOnErrand(123L, MUNICIPALITY_ID, NAMESPACE, statuses);

		// Assert
		assertThat(errand.getStatuses()).isNotEmpty().hasSize(3).allSatisfy(status -> {
			assertThat(status.getDateTime()).isInstanceOf(OffsetDateTime.class).isNotNull();
			assertThat(status.getStatusType()).isInstanceOf(String.class).isNotBlank();
			assertThat(status.getDescription()).isInstanceOf(String.class).isNotBlank();
		});

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(123L, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(any());
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addStatusToErrandTest() {

		// Arrange
		final var errand = createErrandEntity();
		final var newStatus = createStatus();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		statusService.addStatusToErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newStatus);

		// Assert
		assertThat(errand.getStatuses()).isNotEmpty().hasSize(2);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);

		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

}
