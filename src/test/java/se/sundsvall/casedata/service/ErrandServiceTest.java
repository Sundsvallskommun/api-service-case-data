package se.sundsvall.casedata.service;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.MUNICIPALITY_ID;
import static se.sundsvall.casedata.TestUtil.NAMESPACE;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandEntity;
import static se.sundsvall.casedata.TestUtil.createPatchErrand;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;

import com.turkraft.springfilter.converter.FilterSpecificationConverter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.casedata.api.model.Notification;
import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper;

@ExtendWith(MockitoExtension.class)
class ErrandServiceTest {

	@InjectMocks
	private ErrandService errandService;

	@Spy
	private FilterSpecificationConverter filterSpecificationConverterSpy;

	@Mock
	private ErrandRepository errandRepositoryMock;

	@Mock
	private FacilityRepository facilityRepositoryMock;

	@Mock
	private ProcessService processServiceMock;

	@Mock
	private NotificationService notificationServiceMock;

	@Captor
	private ArgumentCaptor<List<Long>> idListCapture;

	@Captor
	private ArgumentCaptor<Notification> notificationCaptor;

	private ErrandEntity mockErrandFindByIdAndMunicipalityIdAndNamespace() {
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		return errand;
	}

	@Test
	void createWhenParkingPermit() {
		// Arrange
		final var inputErrandDTO = createErrand();
		inputErrandDTO.setCaseType(PARKING_PERMIT.name());
		final var inputErrand = toErrandEntity(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		final var processId = UUID.randomUUID().toString();
		when(processServiceMock.startProcess(inputErrand)).thenReturn(processId);

		// Act
		errandService.create(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock, times(2)).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void findByIdAndMunicipalityIdAndNamespace() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityIdAndNamespace();

		// Act
		errandService.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void findByIdAndMunicipalityIdAndNamespaceNotFound() {

		// Arrange
		final var errandDTO = createErrand();
		final var errand = toErrandEntity(errandDTO, MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.empty());

		final var id = errand.getId();

		// Act
		final var problem = assertThrows(ThrowableProblem.class, () -> errandService.findByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(problem.getStatus()).isEqualTo(NOT_FOUND);
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void delete() {

		// Arrange
		final var id = 1L;
		final var entity = createErrandEntity();
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));

		// Act
		errandService.delete(id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).delete(entity);
	}

	@Test
	void deleteWhenNotFound() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).thenReturn(empty());

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> errandService.delete(id, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception.getMessage()).isEqualTo("Not Found: Errand with id:'1' not found in namespace:'MY_NAMESPACE' for municipality with id:'2281'");
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock, never()).delete(any(ErrandEntity.class));
	}

	@Test
	void update() {

		// Arrange
		final var errand = createErrandEntity();
		final var updatedErrand = createErrandEntity();
		final var patch = createPatchErrand();
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(errand)).thenReturn(updatedErrand);

		// Act
		errandService.update(errand.getId(), MUNICIPALITY_ID, NAMESPACE, patch);

		// Assert
		assertThat(errand).satisfies(e -> {
			assertThat(e.getCaseType()).isEqualTo(patch.getCaseType());
			assertThat(e.getExternalCaseId()).isEqualTo(patch.getExternalCaseId());
			assertThat(e.getPriority()).isEqualTo(patch.getPriority());
			assertThat(e.getDescription()).isEqualTo(patch.getDescription());
			assertThat(e.getCaseTitleAddition()).isEqualTo(patch.getCaseTitleAddition());
			assertThat(e.getDiaryNumber()).isEqualTo(patch.getDiaryNumber());
			assertThat(e.getPhase()).isEqualTo(patch.getPhase());
			assertThat(e.getStartDate()).isEqualTo(patch.getStartDate());
			assertThat(e.getEndDate()).isEqualTo(patch.getEndDate());
			assertThat(e.getExtraParameters())
				.containsAll(patch.getExtraParameters().stream()
					.map(parameter -> ErrandExtraParameterMapper.toErrandParameterEntity(parameter)
						.withErrand(errand))
					.toList());
		});

		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(updatedErrand));

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Ärende uppdaterat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(errand.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(errand.getId());
	}

	@Test
	void findAllWithoutDuplicates() {

		// Arrange
		final var errandDTO = createErrand();
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(dto -> EntityMapper.toErrandEntity(dto, MUNICIPALITY_ID, NAMESPACE))
			.toList();

		returnErrands.forEach(
			errandEntity -> errandEntity.setId(errandDTO.getId()));

		when(errandRepositoryMock.findAll(ArgumentMatchers.<Specification<ErrandEntity>>any())).thenReturn(returnErrands);
		when(errandRepositoryMock.findAllByIdInAndMunicipalityIdAndNamespace(anyList(), eq(MUNICIPALITY_ID), eq(NAMESPACE), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(returnErrands.getFirst())));

		final Specification<ErrandEntity> filterSpecification = filterSpecificationConverterSpy.convert("stakeholders.firstName '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*'");
		final Pageable pageable = PageRequest.of(0, 20);

		// Act
		errandService.findAll(filterSpecification, MUNICIPALITY_ID, NAMESPACE, pageable);

		// Assert
		verify(errandRepositoryMock).findAllByIdInAndMunicipalityIdAndNamespace(idListCapture.capture(), eq(MUNICIPALITY_ID), eq(NAMESPACE), any(Pageable.class));

		assertThat(idListCapture.getValue()).hasSize(1);
		assertThat(idListCapture.getValue().getFirst()).isEqualTo(errandDTO.getId());
	}

	@Test
	void findAllWithoutNamespaceAndDuplicates() {

		// Arrange
		final var errandDTO = createErrand();
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(dto -> EntityMapper.toErrandEntity(dto, MUNICIPALITY_ID, NAMESPACE))
			.toList();

		returnErrands.forEach(
			errandEntity -> errandEntity.setId(errandDTO.getId()));

		when(errandRepositoryMock.findAll(ArgumentMatchers.<Specification<ErrandEntity>>any())).thenReturn(returnErrands);
		when(errandRepositoryMock.findAllByIdInAndMunicipalityId(anyList(), eq(MUNICIPALITY_ID), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(returnErrands.getFirst())));

		final Specification<ErrandEntity> filterSpecification = filterSpecificationConverterSpy.convert("stakeholders.firstName '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*'");
		final Pageable pageable = PageRequest.of(0, 20);

		// Act
		errandService.findAllWithoutNamespace(filterSpecification, MUNICIPALITY_ID, pageable);

		// Assert
		verify(errandRepositoryMock).findAllByIdInAndMunicipalityId(idListCapture.capture(), eq(MUNICIPALITY_ID), any(Pageable.class));

		assertThat(idListCapture.getValue()).hasSize(1);
		assertThat(idListCapture.getValue().getFirst()).isEqualTo(errandDTO.getId());
	}

	@Test
	void updateWhenParkingPermit() {

		// Arrange
		final var dto = new PatchErrand();
		final var entity = new ErrandEntity();
		entity.setCaseType(PARKING_PERMIT_RENEWAL.name());
		when(errandRepositoryMock.findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		errandService.update(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(errandRepositoryMock).findWithPessimisticLockingByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(entity);
		verify(notificationServiceMock).create(eq(MUNICIPALITY_ID), eq(NAMESPACE), notificationCaptor.capture(), same(entity));

		assertThat(notificationCaptor.getValue().getDescription()).isEqualTo("Ärende uppdaterat");
		assertThat(notificationCaptor.getValue().getType()).isEqualTo("UPDATE");
		assertThat(notificationCaptor.getValue().getCreatedBy()).isEqualTo(entity.getCreatedBy());
		assertThat(notificationCaptor.getValue().getErrandId()).isEqualTo(entity.getId());
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

}
