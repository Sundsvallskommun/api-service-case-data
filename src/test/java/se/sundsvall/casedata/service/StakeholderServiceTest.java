package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.DRIVER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.OPERATOR;
import static se.sundsvall.casedata.integration.db.model.enums.StakeholderType.ORGANIZATION;
import static se.sundsvall.casedata.integration.db.model.enums.StakeholderType.PERSON;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.casedata.api.model.StakeholderDTO;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.Stakeholder;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class StakeholderServiceTest {

	@Mock
	private StakeholderRepository stakeholderRepository;

	@InjectMocks
	private StakeholderService stakeholderService;


	@Test
	void findAllStakeholdersByMunicipalityId() {
		final List<Stakeholder> stakeholders = List.of(createStakeholder(), createStakeholder());
		when(stakeholderRepository.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(stakeholders);

		final var result = stakeholderService.findAllStakeholdersByMunicipalityId(MUNICIPALITY_ID);

		assertThat(result).hasSize(2);

		verify(stakeholderRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
		verifyNoMoreInteractions(stakeholderRepository);
	}

	@Test
	void findAllStakeholdersByMunicipalityId404() {
		when(stakeholderRepository.findAllByMunicipalityId(MUNICIPALITY_ID)).thenReturn(List.of());

		assertThatThrownBy(() -> stakeholderService.findAllStakeholdersByMunicipalityId(MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);

		verify(stakeholderRepository).findAllByMunicipalityId(MUNICIPALITY_ID);
		verifyNoMoreInteractions(stakeholderRepository);
	}

	@Test
	void findStakeholdersByRoleAndMunicipalityId() {
		final List<Stakeholder> stakeholders = Stream.of(
				createStakeholderDTO(ORGANIZATION, List.of(DRIVER.name()), MUNICIPALITY_ID),
				createStakeholderDTO(PERSON, List.of(DRIVER.name(), OPERATOR.name()), MUNICIPALITY_ID))
			.map(stakeholderDTO -> toStakeholder(stakeholderDTO, MUNICIPALITY_ID))
			.toList();
		when(stakeholderRepository.findByRolesAndMunicipalityId(DRIVER.name(), MUNICIPALITY_ID)).thenReturn(stakeholders);

		final var result = stakeholderService.findStakeholdersByRoleAndMunicipalityId(DRIVER.name(), MUNICIPALITY_ID);

		assertThat(result).hasSize(2);

		verify(stakeholderRepository).findByRolesAndMunicipalityId(DRIVER.name(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(stakeholderRepository);
	}

	@Test
	void findStakeholdersByRoleAndMunicipalityId404() {
		when(stakeholderRepository.findByRolesAndMunicipalityId(DRIVER.name(), MUNICIPALITY_ID)).thenReturn(List.of());

		assertThatThrownBy(() -> stakeholderService.findStakeholdersByRoleAndMunicipalityId(DRIVER.name(), MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);

		verify(stakeholderRepository).findByRolesAndMunicipalityId(DRIVER.name(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(stakeholderRepository);
	}

	@Test
	void testFindByIdAndMunicipalityId() {
		final var stakeholder = toStakeholder(createStakeholderDTO(PERSON, List.of(StakeholderRole.APPLICANT.name()), MUNICIPALITY_ID), MUNICIPALITY_ID);
		when(stakeholderRepository.findByIdAndMunicipalityId(anyLong(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(stakeholder));

		final var result = stakeholderService.findByIdAndMunicipalityId(5L, MUNICIPALITY_ID);

		assertThat(result).isEqualTo(toStakeholderDto(stakeholder));

		verify(stakeholderRepository).findByIdAndMunicipalityId(5L, MUNICIPALITY_ID);
		verifyNoMoreInteractions(stakeholderRepository);
	}

	@Test
	void testFindByIdAndMunicipalityIdNotFound() {
		when(stakeholderRepository.findByIdAndMunicipalityId(anyLong(), eq(MUNICIPALITY_ID))).thenReturn(Optional.empty());

		assertThatThrownBy(() -> stakeholderService.findByIdAndMunicipalityId(3L, MUNICIPALITY_ID))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);

		verify(stakeholderRepository).findByIdAndMunicipalityId(3L, MUNICIPALITY_ID);
		verifyNoMoreInteractions(stakeholderRepository);
	}

	@Test
	void testPut() {
		final var stakeholder = createStakeholder();
		final var stakeholderDto = createStakeholderDTO(PERSON, List.of(StakeholderRole.APPLICANT.name()), MUNICIPALITY_ID);

		when(stakeholderRepository.findByIdAndMunicipalityId(any(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(stakeholder));

		stakeholderService.put(stakeholder.getId(), MUNICIPALITY_ID, stakeholderDto);

		assertThat(stakeholder).satisfies(s -> {
			assertThat(s.getExtraParameters()).isEqualTo(stakeholderDto.getExtraParameters());
			assertThat(s.getType()).isEqualTo(stakeholderDto.getType());
			assertThat(s.getFirstName()).isEqualTo(stakeholderDto.getFirstName());
			assertThat(s.getLastName()).isEqualTo(stakeholderDto.getLastName());
			assertThat(s.getPersonId()).isEqualTo(stakeholderDto.getPersonId());
			assertThat(s.getOrganizationName()).isEqualTo(stakeholderDto.getOrganizationName());
			assertThat(s.getOrganizationNumber()).isEqualTo(stakeholderDto.getOrganizationNumber());
			assertThat(s.getAuthorizedSignatory()).isEqualTo(stakeholderDto.getAuthorizedSignatory());
			assertThat(s.getAdAccount()).isEqualTo(stakeholderDto.getAdAccount());
			assertThat(s.getRoles()).isEqualTo(stakeholderDto.getRoles());
			assertThat(s.getAddresses()).isEqualTo(stakeholderDto.getAddresses().stream().map(EntityMapper::toAddress).toList());
			assertThat(s.getContactInformation()).isEqualTo(stakeholderDto.getContactInformation().stream().map(EntityMapper::toContactInformation).toList());
		});

		verify(stakeholderRepository).findByIdAndMunicipalityId(stakeholder.getId(), MUNICIPALITY_ID);
		verify(stakeholderRepository).save(stakeholder);
		verifyNoMoreInteractions(stakeholderRepository);
	}

	@Test
	void testPatch() {
		final StakeholderDTO stakeholderDTO = new StakeholderDTO();
		final Stakeholder entity = new Stakeholder();
		when(stakeholderRepository.findByIdAndMunicipalityId(anyLong(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(entity));

		stakeholderService.patch(1L, MUNICIPALITY_ID, stakeholderDTO);

		verify(stakeholderRepository).findByIdAndMunicipalityId(1L, MUNICIPALITY_ID);
		verify(stakeholderRepository).save(entity);
		verifyNoMoreInteractions(stakeholderRepository);
	}

}
