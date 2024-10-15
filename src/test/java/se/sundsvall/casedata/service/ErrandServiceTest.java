package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
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
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.ANMALAN_ATTEFALL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrandEntity;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import com.turkraft.springfilter.converter.FilterSpecificationConverter;
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

import se.sundsvall.casedata.api.model.PatchErrand;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.ErrandEntity;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;
import se.sundsvall.casedata.service.util.mappers.ErrandExtraParameterMapper;

import generated.se.sundsvall.parkingpermit.StartProcessResponse;

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

	@Captor
	private ArgumentCaptor<List<Long>> idListCapture;

	@Captor
	private ArgumentCaptor<ErrandEntity> errandCaptor;

	private ErrandEntity mockErrandFindByIdAndMunicipalityIdAndNamespace() {
		final var errand = toErrandEntity(createErrand(), MUNICIPALITY_ID, NAMESPACE);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(any(), eq(MUNICIPALITY_ID), eq(NAMESPACE))).thenReturn(Optional.of(errand));
		return errand;
	}

	@Test
	void postWhenParkingPermit() {
		// Arrange
		final var inputErrandDTO = createErrand();
		inputErrandDTO.setCaseType(PARKING_PERMIT.name());
		final var inputErrand = toErrandEntity(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		final var startProcessResponse = new StartProcessResponse();
		startProcessResponse.setProcessId(UUID.randomUUID().toString());
		when(processServiceMock.startProcess(inputErrand)).thenReturn(startProcessResponse);

		// Act
		errandService.createErrand(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock, times(2)).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void postWhenAnmalanAttefall() {
		// Arrange
		final var inputErrandDTO = createErrand();
		inputErrandDTO.setCaseType(ANMALAN_ATTEFALL.name());
		final var inputErrand = toErrandEntity(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		when(processServiceMock.startProcess(inputErrand)).thenReturn(null);

		// Act
		errandService.createErrand(inputErrandDTO, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock).save(any());
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
	void deleteByIdAndMunicipalityIdAndNamespace() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).thenReturn(true);

		// Act
		errandService.deleteByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);

		// Assert
		verify(errandRepositoryMock).existsByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).deleteByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);
	}

	@Test
	void deleteByIdAndMunicipalityIdAndNamespaceNotFound() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE)).thenReturn(false);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> errandService.deleteByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE));

		// Assert
		assertThat(exception.getMessage()).isEqualTo("Not Found: Errand with id: 1 was not found");
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);

		verify(errandRepositoryMock).existsByIdAndMunicipalityIdAndNamespace(id, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock, never()).deleteById(id);
	}


	@Test
	void updateErrandTest() {

		// Arrange
		final var errand = createErrandEntity();
		final var patch = createPatchErrand();
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(errand));

		// Act
		errandService.updateErrand(errand.getId(), MUNICIPALITY_ID, NAMESPACE, patch);

		// Assert
		assertThat(errand).satisfies(e -> {
			assertThat(e.getCaseType()).isEqualTo(patch.getCaseType().name());
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
						.withErrandEntity(errand))
					.toList());
		});

		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(errand.getId(), MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}


	@Test
	void findAllWithoutDuplicates() {

		// Arrange
		final var errandDTO = createErrand();
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(dto -> EntityMapper.toErrandEntity(dto, MUNICIPALITY_ID, NAMESPACE))
			.toList();

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
	void testPatch() {

		// Arrange
		final var dto = new PatchErrand();
		final var entity = new ErrandEntity();
		entity.setCaseType(PARKING_PERMIT_RENEWAL.name());
		when(errandRepositoryMock.findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		errandService.updateErrand(1L, MUNICIPALITY_ID, NAMESPACE, dto);

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityIdAndNamespace(1L, MUNICIPALITY_ID, NAMESPACE);
		verify(errandRepositoryMock).save(entity);
		verify(processServiceMock).updateProcess(entity);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}


}
