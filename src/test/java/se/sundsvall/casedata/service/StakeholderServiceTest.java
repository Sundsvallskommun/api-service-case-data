package se.sundsvall.casedata.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.TestUtil;
import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;

@ExtendWith(MockitoExtension.class)
class StakeholderServiceTest {

	@Mock
	private StakeholderRepository stakeholderRepository;

	@InjectMocks
	private StakeholderService stakeholderService;


	@Test
	void findAllStakeholders() {
		final List<Stakeholder> stakeholderList = List.of(createStakeholder(), createStakeholder());
		doReturn(stakeholderList).when(stakeholderRepository).findAll();

		final List<StakeholderDTO> resultList = stakeholderService.findAllStakeholders();
		Assertions.assertEquals(2, resultList.size());
		verify(stakeholderRepository, times(1)).findAll();
	}

	@Test
	void findAllStakeholders404() {
		final List<Stakeholder> stakeholderList = new ArrayList<>();
		doReturn(stakeholderList).when(stakeholderRepository).findAll();

		final ThrowableProblem problem = Assertions.assertThrows(ThrowableProblem.class, () -> stakeholderService.findAllStakeholders());
		Assertions.assertEquals(Status.NOT_FOUND, problem.getStatus());
		verify(stakeholderRepository, times(1)).findAll();
	}

	@Test
	void findStakeholdersByRole() {
		final List<Stakeholder> stakeholderList = Stream.of(
				TestUtil.createStakeholderDTO(StakeholderType.ORGANIZATION, List.of(StakeholderRole.DRIVER)),
				TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.DRIVER, StakeholderRole.OPERATOR)))
			.map(EntityMapper::toStakeholder)
			.toList();

		doReturn(stakeholderList).when(stakeholderRepository).findByRoles(StakeholderRole.DRIVER.name());

		final List<StakeholderDTO> resultList = stakeholderService.findStakeholdersByRole(StakeholderRole.DRIVER);
		Assertions.assertEquals(2, resultList.size());
		verify(stakeholderRepository, times(1)).findByRoles(StakeholderRole.DRIVER.name());
	}

	@Test
	void findStakeholdersByRole404() {
		final List<Stakeholder> stakeholderList = new ArrayList<>();

		doReturn(stakeholderList).when(stakeholderRepository).findByRoles(StakeholderRole.DRIVER.name());

		final ThrowableProblem problem = Assertions.assertThrows(ThrowableProblem.class, () -> stakeholderService.findStakeholdersByRole(StakeholderRole.DRIVER));
		Assertions.assertEquals(Status.NOT_FOUND, problem.getStatus());
		verify(stakeholderRepository, times(1)).findByRoles(StakeholderRole.DRIVER.name());
	}

	@Test
	void testFindById() {
		final Long id = new Random().nextLong();
		final var stakeholder = toStakeholder(TestUtil.createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT)));
		Mockito.doReturn(Optional.of(stakeholder)).when(stakeholderRepository).findById(id);

		final var result = stakeholderService.findById(id);
		assertEquals(toStakeholderDto(stakeholder), result);

		verify(stakeholderRepository, times(1)).findById(id);
	}

	@Test
	void testFindByIdNotFound() {
		final Long id = new Random().nextLong();
		Mockito.doReturn(Optional.empty()).when(stakeholderRepository).findById(id);

		final var problem = assertThrows(ThrowableProblem.class, () -> stakeholderService.findById(id));

		assertEquals(Status.NOT_FOUND, problem.getStatus());
		verify(stakeholderRepository, times(1)).findById(id);
	}

	@Test
	void testPut() {
		var stakeholder = createStakeholder();
		var stakeholderDto = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.APPLICANT));

		when(stakeholderRepository.findById(any())).thenReturn(Optional.of(stakeholder));

		stakeholderService.put(stakeholder.getId(), stakeholderDto);

		assertThat(stakeholder)
			.usingRecursiveComparison()
			.ignoringFieldsMatchingRegexes("id", "version", "created", "updated")
			.isEqualTo(toStakeholder(stakeholderDto));
	}

	@Test
	void testPatch() {
		// Mocking the getStakeholder method to return a sample Stakeholder entity
		final Long stakeholderId = 1L;
		final StakeholderDTO stakeholderDTO = new StakeholderDTO();
		final Stakeholder entity = new Stakeholder();
		when(stakeholderRepository.findById(stakeholderId)).thenReturn(Optional.of(entity));

		// No exception is thrown when save() is called on the repository
		stakeholderService.patch(stakeholderId, stakeholderDTO);

		// Verify that the repository's save() method was called once
		verify(stakeholderRepository, times(1)).save(entity);
		verifyNoMoreInteractions(stakeholderRepository);
	}
}
