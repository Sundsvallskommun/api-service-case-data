package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createAddress;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createExtraParameters;
import static se.sundsvall.casedata.TestUtil.createFacility;
import static se.sundsvall.casedata.TestUtil.createFacilityEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import se.sundsvall.casedata.api.model.Facility;
import se.sundsvall.casedata.api.model.validation.enums.FacilityType;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.AddressEntity;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.integration.db.model.enums.AddressCategory;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

@ExtendWith(MockitoExtension.class)
class FacilityServiceTest {

	@InjectMocks
	private FacilityService facilityService;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private FacilityRepository facilityRepositoryMock;

	@Mock
	private ApplicationEventPublisher applicationEventPublisherMock;

	private ErrandEntity mockErrandFindByIdAndMunicipalityIdAndNamespace() {
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		return errand;
	}

	@Test
	void create() {

		// Arrange
		final var errand = createErrandEntity();
		final var errandId = errand.getId();
		final var facility = Facility.builder()
			.withMunicipalityId(MUNICIPALITY_ID)
			.withNamespace(NAMESPACE)
			.withDescription("description")
			.withExtraParameters(createExtraParameters())
			.withAddress(createAddress(AddressCategory.VISITING_ADDRESS))
			.withFacilityType(FacilityType.GARAGE.name())
			.withFacilityCollectionName("facilityCollectionName")
			.withMainFacility(true)
			.build();

		final var facilityEntity = toFacilityEntity(facility, MUNICIPALITY_ID, NAMESPACE);

		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(facilityRepositoryMock.saveAndFlush(any())).thenReturn(facilityEntity);

		// Act
		final var result = facilityService.create(errandId, MUNICIPALITY_ID, NAMESPACE, facility);

		// Assert
		assertThat(result).isEqualTo(facility);
		verify(applicationEventPublisherMock).publishEvent(errand);
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(facilityRepositoryMock).saveAndFlush(any());
		verifyNoMoreInteractions(applicationEventPublisherMock, errandRepositoryMock);
	}

	@Test
	void findFacility() {

		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facility = createFacilityEntity();
		when(facilityRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(facility));

		// Act
		final var result = facilityService.findFacility(errandId, facilityId, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(result).isEqualTo(toFacility(facility));
		verify(facilityRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock, facilityRepositoryMock);
	}

	@Test
	void findFacilities() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityIdAndNamespace();

		// Act
		final var result = facilityService.findFacilities(errand.getId(), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		assertThat(errand.getFacilities().stream().map(EntityMapper::toFacility).toList()).isEqualTo(result);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verifyNoMoreInteractions(errandRepositoryMock, facilityRepositoryMock);
	}

	@Test
	void replaceFacilities() {

		// Arrange
		final var errandId = 123L;
		final var facilityId_1 = 456L;
		final var facilityId_2 = 789L;
		final var errand = createErrandEntity(); // Errand with one facility
		errand.getFacilities().add(createFacilityEntity()); // Add another facility to the errand. This will be removed

		errand.getFacilities().getFirst().setId(facilityId_1);

		final var facility1 = createFacility();
		facility1.setId(facilityId_1);
		final var facility2 = createFacility();
		facility2.setId(facilityId_2);

		final var facilities = List.of(facility1, facility2, createFacility());

		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(any(Long.class), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		facilityService.replaceFacilities(errandId, MUNICIPALITY_ID, NAMESPACE, facilities);

		// Assert
		assertThat(errand.getFacilities()).isNotEmpty().hasSize(3).allSatisfy(facility -> {
			assertThat(facility.getFacilityType()).isInstanceOf(String.class).isNotNull();
			assertThat(facility.isMainFacility()).isInstanceOf(Boolean.class).isNotNull();
			assertThat(facility.getDescription()).isInstanceOf(String.class).isNotBlank();
			assertThat(facility.getAddress()).isInstanceOf(AddressEntity.class).isNotNull();
			assertThat(facility.getExtraParameters()).isInstanceOf(HashMap.class).isNotNull();
			assertThat(facility.getFacilityCollectionName()).isInstanceOf(String.class).isNotBlank();
			assertThat(facility.getVersion()).isInstanceOf(Integer.class).isNotNull();
		});

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(any());
		verify(applicationEventPublisherMock).publishEvent(errand);
	}

	@Test
	void delete() {

		// Arrange
		final var errand = createErrandEntity();
		final var errandId = errand.getId();
		final var facility = createFacilityEntity();
		final var facilityId = facility.getId();

		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		facilityService.delete(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).saveAndFlush(errand);
		verify(applicationEventPublisherMock).publishEvent(errand);
		assertThat(errand.getFacilities()).doesNotContain(facility);
		verifyNoMoreInteractions(errandRepositoryMock, applicationEventPublisherMock);
	}

	@Test
	void update() {

		// Arrange
		final var errand = createErrandEntity();
		final var errandId = errand.getId();
		final var facility = createFacilityEntity();
		final var facilityId = facility.getId();
		final var patch = createFacility();

		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(facilityRepositoryMock.findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(facility));
		when(facilityRepositoryMock.saveAndFlush(facility)).thenReturn(facility);

		// Act
		final var result = facilityService.update(errandId, MUNICIPALITY_ID, NAMESPACE, facilityId, patch);

		// Assert
		assertThat(result).isNotNull().satisfies(f -> {
			assertThat(f.getAddress()).isEqualTo(patch.getAddress());
			assertThat(f.getDescription()).isEqualTo(patch.getDescription());
			assertThat(f.getFacilityCollectionName()).isEqualTo(patch.getFacilityCollectionName());
			assertThat(f.getFacilityType()).isEqualTo(patch.getFacilityType());
			assertThat(f.isMainFacility()).isEqualTo(patch.isMainFacility());
			assertThat(f.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(facilityRepositoryMock).findByIdAndErrandIdAndMunicipalityIdAndNamespace(facilityId, errandId, MUNICIPALITY_ID, NAMESPACE);
		verify(facilityRepositoryMock).saveAndFlush(facility);
		verify(applicationEventPublisherMock).publishEvent(errand);
		verifyNoMoreInteractions(errandRepositoryMock, facilityRepositoryMock, applicationEventPublisherMock);
	}
}
