package se.sundsvall.casedata.integration.db;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.ErrandService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;

@SpringBootTest
@ActiveProfiles("junit")
class OptimisticLockingTest {

	@MockBean
	private ErrandRepository errandRepositoryMock;

	@Autowired
	private ErrandService errandService;

	@Test
	void patchErrandWithStakeholderOptimisticLockingFailureException() {
		final var errand = createErrand();
		final var stakeholderDto = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER.name()));
		when(errandRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(errand));
		doThrow(OptimisticLockingFailureException.class).when(errandRepositoryMock).save(any());

		assertThatThrownBy(() -> errandService.addStakeholderToErrand(123L, stakeholderDto))
			.isInstanceOf(OptimisticLockingFailureException.class);

		//5 invocations because @Retry.
		verify(errandRepositoryMock, times(5)).save(any(Errand.class));
	}

	@Test
	void patchErrandWithStakeholderOtherException() {
		final var errand = createErrand();
		final var stakeholderDto = createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER.name()));
		when(errandRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(errand));
		doThrow(RuntimeException.class).when(errandRepositoryMock).save(any());

		assertThatThrownBy(() -> errandService.addStakeholderToErrand(123L, stakeholderDto))
			.isInstanceOf(RuntimeException.class);

		//Only 1 invocation, not retrying because it's not an OptimisticLockingFailureException.
		verify(errandRepositoryMock, times(1)).save(any(Errand.class));
	}

}
