package se.sundsvall.casedata.integration.db;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createStakeholder;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.StakeholderService;

@SpringBootTest
@ActiveProfiles("junit")
class OptimisticLockingTest {

	@MockBean
	private ErrandRepository errandRepositoryMock;

	@Autowired
	private StakeholderService stakeholderService;

	@Test
	void patchErrandWithStakeholderOptimisticLockingFailureException() {
		final var errand = createErrandEntity();
		final var stakeholderDto = createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER.name()));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		doThrow(OptimisticLockingFailureException.class).when(errandRepositoryMock).save(any());

		assertThatThrownBy(() -> stakeholderService.addStakeholderToErrand(123L, MUNICIPALITY_ID, NAMESPACE, stakeholderDto))
			.isInstanceOf(OptimisticLockingFailureException.class);

		// 5 invocations because @Retry.
		verify(errandRepositoryMock, times(5)).save(any(ErrandEntity.class));
	}

	@Test
	void patchErrandWithStakeholderOtherException() {
		final var errand = createErrandEntity();
		final var stakeholderDto = createStakeholder(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER.name()));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		doThrow(RuntimeException.class).when(errandRepositoryMock).save(any());

		assertThatThrownBy(() -> stakeholderService.addStakeholderToErrand(123L, MUNICIPALITY_ID, NAMESPACE, stakeholderDto))
			.isInstanceOf(RuntimeException.class);

		// Only 1 invocation, not retrying because it's not an OptimisticLockingFailureException.
		verify(errandRepositoryMock).save(any(ErrandEntity.class));
	}

}
