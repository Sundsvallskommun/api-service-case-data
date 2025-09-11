package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAddress;
import static se.sundsvall.casedata.TestUtil.createContactInformation;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createStakeholder;
import static se.sundsvall.casedata.TestUtil.createStakeholderEntity;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderRole;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderType;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.DRIVER;
import static se.sundsvall.casedata.api.model.validation.enums.StakeholderRole.OPERATOR;
import static se.sundsvall.casedata.integration.db.model.enums.StakeholderType.ORGANIZATION;
import static se.sundsvall.casedata.integration.db.model.enums.StakeholderType.PERSON;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholderEntity;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.Stakeholder;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.StakeholderRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.StakeholderEntity;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.integration.db.model.enums.ContactType;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class StakeholderServiceTest {

	@Mock
	private StakeholderRepository stakeholderRepositoryMock;

	@InjectMocks
	private StakeholderService stakeholderService;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private ApplicationEventPublisher applicationEventPublisherMock;

	@Captor
	private ArgumentCaptor<ErrandEntity> errandCaptor;

	@Test
	void findStakeholders() {
		// Arrange
		final List<StakeholderEntity> stakeholders = List.of(createStakeholderEntity(), createStakeholderEntity());
		final var errand = createErrandEntity();
		errand.setStakeholders(stakeholders);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		final var result = stakeholderService.findStakeholders(1L, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE));
		verifyNoMoreInteractions(errandRepositoryMock);
		verifyNoInteractions(stakeholderRepositoryMock);
	}

	@Test
	void findStakeholdersNotFound() {

		// Arrange
		final var errand = createErrandEntity();
		errand.setStakeholders(null);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		// Act
		final var result = stakeholderService.findStakeholders(1L, MUNICIPALITY_ID, NAMESPACE);
		// Assert
		assertThat(result).isEmpty();
		verifyNoMoreInteractions(errandRepositoryMock);
		verifyNoInteractions(stakeholderRepositoryMock);
	}

	@Test
	void findStakeholdersByRole() {
		// Arrange
		final var stakeholders = Stream.of(
			createStakeholder(ORGANIZATION, List.of(DRIVER.name())),
			createStakeholder(PERSON, List.of(DRIVER.name(), OPERATOR.name())))
			.map(stakeholderDTO -> toStakeholderEntity(stakeholderDTO, MUNICIPALITY_ID, NAMESPACE))
			.toList();
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setStakeholders(stakeholders);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		final var result = stakeholderService.findStakeholdersByRole(1L, DRIVER.name(), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE));
		verifyNoMoreInteractions(stakeholderRepositoryMock);
	}

	@Test
	void findStakeholdersByRoled404() {
		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		final var driverName = DRIVER.name();
		errand.setStakeholders(null);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		final var result = stakeholderService.findStakeholdersByRole(1L, driverName, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEmpty();
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE));
		verifyNoMoreInteractions(stakeholderRepositoryMock);
	}

	@Test
	void findStakeholder() {
		// Arrange
		final var stakeholderId = 5L;
		final var errandId = 1L;
		final var stakeholder = toStakeholderEntity(createStakeholder(PERSON, List.of(StakeholderRole.APPLICANT.name())), MUNICIPALITY_ID, NAMESPACE);
		stakeholder.setId(stakeholderId);
		final var errand = createErrandEntity();
		errand.setStakeholders(List.of(stakeholder));
		errand.setId(errandId);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		final var result = stakeholderService.findStakeholder(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEqualTo(toStakeholder(stakeholder));

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock);
		verifyNoInteractions(stakeholderRepositoryMock);
	}

	@Test
	void findStakeholderNotFound() {
		// Arrange
		final var errand = createErrandEntity();
		errand.setStakeholders(null);
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act & Assert
		assertThatThrownBy(() -> stakeholderService.findStakeholder(1L, 3L, MUNICIPALITY_ID, NAMESPACE))
			.isInstanceOf(ThrowableProblem.class)
			.hasFieldOrPropertyWithValue("status", Status.NOT_FOUND);

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(stakeholderRepositoryMock);
	}

	@Test
	void replaceOnErrand() {
		// Arrange
		final var stakeholder = createStakeholderEntity();
		final var stakeholderDto = createStakeholder(PERSON, List.of(StakeholderRole.APPLICANT.name()));
		final var errand = createErrandEntity();
		errand.setStakeholders(List.of(stakeholder));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		stakeholderService.replaceOnErrand(1L, stakeholder.getId(), MUNICIPALITY_ID, NAMESPACE, stakeholderDto);

		// Assert
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
			assertThat(s.getAddresses()).isEqualTo(stakeholderDto.getAddresses().stream().map(EntityMapper::toAddressEntity).toList());
			assertThat(s.getContactInformation()).isEqualTo(stakeholderDto.getContactInformation().stream().map(EntityMapper::toContactInformationEntity).toList());
		});

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(stakeholderRepositoryMock).save(stakeholder);
		verifyNoMoreInteractions(stakeholderRepositoryMock);
	}

	@Test
	void update() {

		// Arrange
		final var stakeholderId = 1L;
		final var errandId = 1L;
		final var stakeholder = Stakeholder.builder().withId(stakeholderId).build();
		final var entity = new StakeholderEntity();
		entity.setId(stakeholderId);
		final var errand = createErrandEntity();
		errand.setStakeholders(List.of(entity));
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(anyLong(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));

		// Act
		stakeholderService.update(errandId, stakeholderId, MUNICIPALITY_ID, NAMESPACE, stakeholder);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(stakeholderRepositoryMock).save(entity);
		verifyNoMoreInteractions(stakeholderRepositoryMock, errandRepositoryMock);
	}

	@Test
	void replaceStakeholderOnErrand() {

		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		final var stakeholders = List.of(createStakeholder(getRandomStakeholderType(), List.of(getRandomStakeholderRole())));
		final var stakeholderEntities = stakeholders.stream().map(s -> toStakeholderEntity(s, MUNICIPALITY_ID, NAMESPACE)).toList();
		errand.getStakeholders().addAll(stakeholderEntities);
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		stakeholderService.replaceOnErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, stakeholders);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(errandCaptor.capture());
		assertThat(errandCaptor.getValue().getStakeholders()).isNotEmpty().hasSize(1);
		assertThat(errandCaptor.getValue().getStakeholders().getFirst())
			.usingRecursiveComparison()
			.ignoringFields("municipalityId", "errand", "namespace", "created", "updated")
			.isEqualTo(stakeholders.getFirst());

		verify(applicationEventPublisherMock).publishEvent(errand);
		verifyNoMoreInteractions(errandRepositoryMock, applicationEventPublisherMock);
	}

	@Test
	void delete() {

		// Arrange
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());
		// Set ID on every stakeholder
		errand.getStakeholders().forEach(s -> s.setId(new Random().nextLong(1, 1000)));

		final var errandId = new Random().nextLong(1, 1000);
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		final var stakeholder = errand.getStakeholders().getFirst();

		// Act
		stakeholderService.delete(errandId, MUNICIPALITY_ID, NAMESPACE, stakeholder.getId());

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(applicationEventPublisherMock).publishEvent(errand);
		verify(errandRepositoryMock).saveAndFlush(errand);
		verifyNoMoreInteractions(errandRepositoryMock, applicationEventPublisherMock);
	}

	@Test
	void addToErrand() {

		// Arrange
		final var errand = createErrandEntity();
		final var newStakeholder = Stakeholder.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withType(StakeholderType.PERSON)
			.withPersonId(UUID.randomUUID().toString())
			.withAdAccount(RandomStringUtils.secure().next(10, true, false))
			.withFirstName(RandomStringUtils.secure().next(10, true, false))
			.withLastName(RandomStringUtils.secure().next(10, true, false))
			.withRoles(List.of(OPERATOR.name()))
			.withContactInformation(List.of(createContactInformation(ContactType.EMAIL), createContactInformation(ContactType.PHONE), createContactInformation(ContactType.CELLPHONE)))
			.withAddresses(List.of(createAddress(AddressCategory.VISITING_ADDRESS)))
			.withExtraParameters(createExtraParameters())
			.build();
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var stakeholder = stakeholderService.addToErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, newStakeholder);

		// Assert
		assertThat(stakeholder).isEqualTo(newStakeholder);
		assertThat(errand.getStakeholders()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(errand);
		verify(applicationEventPublisherMock).publishEvent(errand);
		verifyNoMoreInteractions(errandRepositoryMock, applicationEventPublisherMock);
	}
}
