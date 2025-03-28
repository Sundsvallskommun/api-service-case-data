package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createStatus;

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
	void addToErrand() {

		// Arrange
		final var errand = createErrandEntity();
		final var newStatus = createStatus();
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		statusService.addToErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newStatus);

		// Assert
		assertThat(errand.getStatuses()).isNotEmpty().hasSize(2);
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);

		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}
}
