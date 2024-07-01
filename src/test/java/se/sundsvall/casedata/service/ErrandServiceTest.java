package se.sundsvall.casedata.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.casedata.TestUtil.createAppealDTO;
import static se.sundsvall.casedata.TestUtil.createDecisionDTO;
import static se.sundsvall.casedata.TestUtil.createErrand;
import static se.sundsvall.casedata.TestUtil.createErrandDTO;
import static se.sundsvall.casedata.TestUtil.createFacility;
import static se.sundsvall.casedata.TestUtil.createFacilityDTO;
import static se.sundsvall.casedata.TestUtil.createNoteDTO;
import static se.sundsvall.casedata.TestUtil.createPatchErrandDto;
import static se.sundsvall.casedata.TestUtil.createStakeholderDTO;
import static se.sundsvall.casedata.TestUtil.createStatusDTO;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderRole;
import static se.sundsvall.casedata.TestUtil.getRandomStakeholderType;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.ANMALAN_ATTEFALL;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT;
import static se.sundsvall.casedata.api.model.validation.enums.CaseType.PARKING_PERMIT_RENEWAL;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toErrand;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacility;
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toFacilityDto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

import se.sundsvall.casedata.api.model.PatchErrandDTO;
import se.sundsvall.casedata.api.model.validation.enums.StakeholderRole;
import se.sundsvall.casedata.integration.db.ErrandRepository;
import se.sundsvall.casedata.integration.db.FacilityRepository;
import se.sundsvall.casedata.integration.db.model.Address;
import se.sundsvall.casedata.integration.db.model.Decision;
import se.sundsvall.casedata.integration.db.model.Errand;
import se.sundsvall.casedata.integration.db.model.enums.StakeholderType;
import se.sundsvall.casedata.service.util.mappers.EntityMapper;

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

	@Test
	void postWhenParkingPermit() {

		// Arrange
		final var inputErrandDTO = createErrandDTO();
		inputErrandDTO.setCaseType(PARKING_PERMIT.name());
		final var inputErrand = toErrand(inputErrandDTO);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		final var startProcessResponse = new StartProcessResponse();
		startProcessResponse.setProcessId(UUID.randomUUID().toString());
		when(processServiceMock.startProcess(inputErrand)).thenReturn(startProcessResponse);

		// Act
		errandService.createErrand(inputErrandDTO);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock, times(2)).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void postWhenAnmalanAttefall() {

		// Arrange
		final var inputErrandDTO = createErrandDTO();
		inputErrandDTO.setCaseType(ANMALAN_ATTEFALL.name());
		final var inputErrand = toErrand(inputErrandDTO);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		when(processServiceMock.startProcess(inputErrand)).thenReturn(null);

		// Act
		errandService.createErrand(inputErrandDTO);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void findById() {

		// Arrange
		final var errand = mockErrandFindById();

		// Act
		errandService.findById(errand.getId());

		// Assert
		verify(errandRepositoryMock, times(1)).findById(errand.getId());
	}

	@Test
	void findByIdNotFound() {

		// Arrange
		final var errandDTO = createErrandDTO();
		final var errand = toErrand(errandDTO);
		errand.setId(new Random().nextLong(1, 1000));
		doReturn(Optional.empty()).when(errandRepositoryMock).findById(any());

		final var id = errand.getId();

		// Act
		final var problem = assertThrows(ThrowableProblem.class, () -> errandService.findById(id));

		// Assert
		assertThat(problem.getStatus()).isEqualTo(NOT_FOUND);
		verify(errandRepositoryMock, times(1)).findById(errand.getId());
	}

	@Test
	void deleteById() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsById(id)).thenReturn(true);

		// Act
		errandService.deleteById(id);

		// Assert
		verify(errandRepositoryMock).existsById(id);
		verify(errandRepositoryMock).deleteById(id);
	}

	@Test
	void deleteByIdNotFound() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsById(id)).thenReturn(false);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> errandService.deleteById(id));

		// Assert
		assertThat(exception.getMessage()).isEqualTo("Not Found: Errand with id: 1 was not found");
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);

		verify(errandRepositoryMock).existsById(id);
		verify(errandRepositoryMock, never()).deleteById(id);
	}

	@Test
	void addStakeholderToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newStakeholder = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.OPERATOR.name()));
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var stakeholder = errandService.addStakeholderToErrand(errand.getId(), newStakeholder);

		// Assert
		assertThat(stakeholder).isEqualTo(newStakeholder);
		assertThat(errand.getStakeholders()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addNoteToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newNote = createNoteDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var note = errandService.addNoteToErrand(errand.getId(), newNote);

		// Assert
		assertThat(note).isEqualTo(newNote);
		assertThat(errand.getNotes()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addStatusToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newStatus = createStatusDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.addStatusToErrand(errand.getId(), newStatus);

		// Assert
		assertThat(errand.getStatuses()).isNotEmpty().hasSize(2);
		verify(errandRepositoryMock).findById(errand.getId());

		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addDecisionToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newDecision = createDecisionDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var decisionDTO = errandService.addDecisionToErrand(errand.getId(), newDecision);

		// Assert
		assertThat(decisionDTO).isEqualTo(newDecision);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addAppealToErrandTest() {

		// Arrange
		final var errand = createErrand();
		errand.getDecisions().add(Decision.builder().withId(123L).build());
		final var newAppeal = createAppealDTO();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var appealDTO = errandService.addAppealToErrand(errand.getId(), newAppeal);

		// Assert
		assertThat(appealDTO).isEqualTo(newAppeal);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
	}

	@Test
	void updateErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var patch = createPatchErrandDto();
		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));

		// Act
		errandService.updateErrand(errand.getId(), patch);

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
			assertThat(e.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});

		verify(errandRepositoryMock).findById(errand.getId());
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void deleteStakeholderOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO());
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());
		// Set ID on every stakeholder
		errand.getStakeholders().forEach(s -> s.setId(new Random().nextLong(1, 1000)));

		final var errandId = new Random().nextLong(1, 1000);
		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		final var stakeholder = errand.getStakeholders().getFirst();

		// Act
		errandService.deleteStakeholderOnErrand(errandId, stakeholder.getId());

		// Assert
		verify(processServiceMock).updateProcess(errand);
		verify(errandRepositoryMock).save(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void replaceStatusesOnErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var statuses = List.of(createStatusDTO(), createStatusDTO(), createStatusDTO());
		when(errandRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.replaceStatusesOnErrand(123L, statuses);

		// Assert
		assertThat(errand.getStatuses()).isNotEmpty().hasSize(3).allSatisfy(status -> {
			assertThat(status.getDateTime()).isInstanceOf(OffsetDateTime.class).isNotNull();
			assertThat(status.getStatusType()).isInstanceOf(String.class).isNotBlank();
			assertThat(status.getDescription()).isInstanceOf(String.class).isNotBlank();
		});

		verify(errandRepositoryMock).findById(123L);
		verify(errandRepositoryMock).save(any());
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void replaceFacilitiesOnErrandTest() {

		// Arrange
		final var errandId = 123L;
		final var facilityId_1 = 456L;
		final var facilityId_2 = 789L;
		final var errand = createErrand(); // Errand with one facility
		errand.getFacilities().add(createFacility()); // Add another facility to the errand. This will be removed

		errand.getFacilities().getFirst().setId(facilityId_1);

		final var facilityDTO_1 = createFacilityDTO();
		facilityDTO_1.setId(facilityId_1);
		final var facilityDTO_2 = createFacilityDTO();
		facilityDTO_2.setId(facilityId_2);

		final var facilities = List.of(facilityDTO_1, facilityDTO_2, createFacilityDTO());

		when(errandRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.replaceFacilitiesOnErrand(errandId, facilities);

		// Assert
		assertThat(errand.getFacilities()).isNotEmpty().hasSize(3).allSatisfy(facility -> {
			assertThat(facility.getFacilityType()).isInstanceOf(String.class).isNotNull();
			assertThat(facility.isMainFacility()).isInstanceOf(Boolean.class).isNotNull();
			assertThat(facility.getDescription()).isInstanceOf(String.class).isNotBlank();
			assertThat(facility.getAddress()).isInstanceOf(Address.class).isNotNull();
			assertThat(facility.getExtraParameters()).isInstanceOf(HashMap.class).isNotNull();
			assertThat(facility.getFacilityCollectionName()).isInstanceOf(String.class).isNotBlank();
			assertThat(facility.getVersion()).isInstanceOf(Integer.class).isNotNull();
			assertThat(facility.getCreated()).isInstanceOf(OffsetDateTime.class).isNotNull();
			assertThat(facility.getUpdated()).isInstanceOf(OffsetDateTime.class).isNotNull();
		});

		verify(errandRepositoryMock).findById(errandId);
		verify(errandRepositoryMock).save(any());
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void replaceStakeholderOnErrandTest() {

		// Arrange
		final var errand = mockErrandFindById();
		final var stakeholders = List.of(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole())));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.replaceStakeholdersOnErrand(errand.getId(), stakeholders);

		// Assert
		assertThat(errand.getStakeholders()).isEqualTo(stakeholders.stream().map(EntityMapper::toStakeholder).toList());

		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void findAllWithoutDuplicates() {

		// Arrange
		final var errandDTO = createErrandDTO();
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(EntityMapper::toErrand)
			.toList();

		doReturn(returnErrands).when(errandRepositoryMock).findAll(ArgumentMatchers.<Specification<Errand>>any());
		doReturn(new PageImpl<>(List.of(returnErrands.getFirst()))).when(errandRepositoryMock).findAllByIdIn(anyList(), any(Pageable.class));

		final Specification<Errand> filterSpecification = filterSpecificationConverterSpy.convert("stakeholders.firstName '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*'");
		final Pageable pageable = PageRequest.of(0, 20);

		// Act
		errandService.findAll(filterSpecification, new HashMap<>(), pageable);

		// Assert
		verify(errandRepositoryMock, times(1)).findAllByIdIn(idListCapture.capture(), any(Pageable.class));

		assertThat(idListCapture.getValue()).hasSize(1);
		assertThat(idListCapture.getValue().getFirst()).isEqualTo(errandDTO.getId());
	}

	@Test
	void getDecisionsOnErrand() {

		// Arrange
		final var errand = mockErrandFindById();

		// Act
		final var result = errandService.findDecisionsOnErrand(errand.getId());

		// Assert
		assertThat(result).isEqualTo(errand.getDecisions().stream().map(EntityMapper::toDecisionDto).toList());
	}

	@Test
	void getDecisionsOnErrandNotFound() {

		// Arrange
		final var errand = toErrand(createErrandDTO());
		errand.setId(new Random().nextLong(1, 1000));
		errand.setDecisions(new ArrayList<>());
		doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());

		final var id = errand.getId();

		// Act/Assert
		assertThrows(ThrowableProblem.class, () -> errandService.findDecisionsOnErrand(id));
	}

	@Test
	void deleteDecisionOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO());
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());

		// Set ID on every decision
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var decision = errand.getDecisions().getFirst();

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteDecisionOnErrand(errandId, decision.getId());

		// Assert
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void deleteNoteOnErrand() {
		// Arrange
		final var errand = toErrand(createErrandDTO());
		errand.setCaseType(ANMALAN_ATTEFALL.name());
		// Set ID on every note
		errand.getNotes().forEach(note -> note.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var note = errand.getNotes().getFirst();

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteNoteOnErrand(errandId, note.getId());

		// Assert
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void deleteAppealOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO());
		// Set ID on every decision
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var appeal = errand.getAppeals().getFirst();
		appeal.setId(new Random().nextLong());

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteAppealOnErrand(errandId, appeal.getId());

		// Assert
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	private Errand mockErrandFindById() {
		final var errand = toErrand(createErrandDTO());
		errand.setId(new Random().nextLong(1, 1000));
		doReturn(Optional.of(errand)).when(errandRepositoryMock).findById(any());
		return errand;
	}

	@Test
	void testPatch() {

		// Arrange
		final var dto = new PatchErrandDTO();
		final var entity = new Errand();
		entity.setCaseType(PARKING_PERMIT_RENEWAL.name());
		when(errandRepositoryMock.findById(1L)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		errandService.updateErrand(1L, dto);

		// Assert
		verify(errandRepositoryMock).save(entity);
		verify(processServiceMock).updateProcess(entity);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void findFacilitiesOnErrand() {

		// Arrange
		final var errand = mockErrandFindById();

		// Act
		final var result = errandService.findFacilitiesOnErrand(errand.getId());

		// Assert
		assertThat(errand.getFacilities().stream().map(EntityMapper::toFacilityDto).toList()).isEqualTo(result);
		verify(errandRepositoryMock).findById(errand.getId());
		verifyNoMoreInteractions(errandRepositoryMock, facilityRepositoryMock);
	}

	@Test
	void findFacilityOnErrand() {

		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facility = createFacility();
		when(facilityRepositoryMock.findByIdAndErrandId(facilityId, errandId)).thenReturn(Optional.of(facility));

		// Act
		final var result = errandService.findFacilityOnErrand(errandId, facilityId);

		// Assert
		assertThat(result).isEqualTo(toFacilityDto(facility));
		verify(facilityRepositoryMock).findByIdAndErrandId(facilityId, errandId);
		verifyNoMoreInteractions(errandRepositoryMock, facilityRepositoryMock);
	}

	@Test
	void createFacilty() {

		// Arrange
		final var errand = createErrand();
		final var errandId = errand.getId();
		final var facilityDto = createFacilityDTO();
		final var facility = toFacility(facilityDto);

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));
		when(facilityRepositoryMock.save(any())).thenReturn(facility);

		// Act
		final var result = errandService.createFacility(errandId, facilityDto);

		// Assert
		assertThat(result).isEqualTo(facilityDto);
		verify(processServiceMock).updateProcess(errand);
		verify(errandRepositoryMock).findById(errandId);
		verify(facilityRepositoryMock).save(facility);
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void deleteFacilityOnErrand() {

		// Arrange
		final var errand = createErrand();
		final var errandId = errand.getId();
		final var facility = createFacility();
		final var facilityId = facility.getId();

		when(errandRepositoryMock.findById(errandId)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteFacilityOnErrand(errandId, facilityId);

		// Assert
		verify(errandRepositoryMock).findById(errandId);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void updateFacilityOnErrand() {

		// Arrange
		final var errand = createErrand();
		final var errandId = errand.getId();
		final var facility = createFacility();
		final var facilityId = facility.getId();
		final var patch = createFacilityDTO();

		when(errandRepositoryMock.findById(errand.getId())).thenReturn(Optional.of(errand));
		when(facilityRepositoryMock.findByIdAndErrandId(facilityId, errandId)).thenReturn(Optional.of(facility));
		when(facilityRepositoryMock.save(facility)).thenReturn(facility);

		// Act
		final var result = errandService.updateFacilityOnErrand(errandId, facilityId, patch);

		// Assert
		assertThat(result).isNotNull().satisfies(f -> {
			assertThat(f.getAddress()).isEqualTo(patch.getAddress());
			assertThat(f.getDescription()).isEqualTo(patch.getDescription());
			assertThat(f.getFacilityCollectionName()).isEqualTo(patch.getFacilityCollectionName());
			assertThat(f.getFacilityType()).isEqualTo(patch.getFacilityType());
			assertThat(f.isMainFacility()).isEqualTo(patch.isMainFacility());
			assertThat(f.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});

		verify(facilityRepositoryMock).findByIdAndErrandId(facilityId, errandId);
		verify(facilityRepositoryMock).save(facility);
		verify(processServiceMock).updateProcess(errand);
	}
}
