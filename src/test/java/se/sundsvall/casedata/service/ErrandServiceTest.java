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
import static se.sundsvall.casedata.service.util.mappers.EntityMapper.toStakeholder;

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
import se.sundsvall.casedata.integration.db.model.Appeal;
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

	@Captor
	private ArgumentCaptor<Errand> errandCaptor;

	@Captor
	private ArgumentCaptor<Appeal> appealCaptor;

	@Test
	void postWhenParkingPermit() {
		// Arrange
		final var inputErrandDTO = createErrandDTO(MUNICIPALITY_ID);
		inputErrandDTO.setCaseType(PARKING_PERMIT.name());
		final var inputErrand = toErrand(inputErrandDTO, MUNICIPALITY_ID);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		final var startProcessResponse = new StartProcessResponse();
		startProcessResponse.setProcessId(UUID.randomUUID().toString());
		when(processServiceMock.startProcess(inputErrand)).thenReturn(startProcessResponse);

		// Act
		errandService.createErrand(inputErrandDTO, MUNICIPALITY_ID);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock, times(2)).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void postWhenAnmalanAttefall() {
		// Arrange
		final var inputErrandDTO = createErrandDTO(MUNICIPALITY_ID);
		inputErrandDTO.setCaseType(ANMALAN_ATTEFALL.name());
		final var inputErrand = toErrand(inputErrandDTO, MUNICIPALITY_ID);
		inputErrand.setId(new Random().nextLong(1, 1000));

		when(errandRepositoryMock.save(any())).thenReturn(inputErrand);
		when(processServiceMock.startProcess(inputErrand)).thenReturn(null);

		// Act
		errandService.createErrand(inputErrandDTO, MUNICIPALITY_ID);

		// Assert
		verify(processServiceMock).startProcess(inputErrand);
		verify(errandRepositoryMock).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void findByIdAndMunicipalityId() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityId();

		// Act
		errandService.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
	}

	@Test
	void findByIdAndMunicipalityIdNotFound() {

		// Arrange
		final var errandDTO = createErrandDTO(MUNICIPALITY_ID);
		final var errand = toErrand(errandDTO, MUNICIPALITY_ID);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityId(any(), eq(MUNICIPALITY_ID))).thenReturn(Optional.empty());

		final var id = errand.getId();

		// Act
		final var problem = assertThrows(ThrowableProblem.class, () -> errandService.findByIdAndMunicipalityId(id, MUNICIPALITY_ID));

		// Assert
		assertThat(problem.getStatus()).isEqualTo(NOT_FOUND);
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
	}

	@Test
	void deleteByIdAndMunicipalityId() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(true);

		// Act
		errandService.deleteByIdAndMunicipalityId(id, MUNICIPALITY_ID);

		// Assert
		verify(errandRepositoryMock).existsByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(errandRepositoryMock).deleteByIdAndMunicipalityId(id, MUNICIPALITY_ID);
	}

	@Test
	void deleteByIdAndMunicipalityIdNotFound() {

		// Arrange
		final var id = 1L;
		when(errandRepositoryMock.existsByIdAndMunicipalityId(id, MUNICIPALITY_ID)).thenReturn(false);

		// Act
		final var exception = assertThrows(ThrowableProblem.class, () -> errandService.deleteByIdAndMunicipalityId(id, MUNICIPALITY_ID));

		// Assert
		assertThat(exception.getMessage()).isEqualTo("Not Found: Errand with id: 1 was not found");
		assertThat(exception.getStatus()).isEqualTo(NOT_FOUND);

		verify(errandRepositoryMock).existsByIdAndMunicipalityId(id, MUNICIPALITY_ID);
		verify(errandRepositoryMock, never()).deleteById(id);
	}

	@Test
	void addStakeholderToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newStakeholder = createStakeholderDTO(StakeholderType.PERSON, List.of(StakeholderRole.OPERATOR.name()), MUNICIPALITY_ID);
		when(errandRepositoryMock.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var stakeholder = errandService.addStakeholderToErrand(errand.getId(), MUNICIPALITY_ID, newStakeholder);

		// Assert
		assertThat(stakeholder).isEqualTo(newStakeholder);
		assertThat(errand.getStakeholders()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addNoteToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newNote = createNoteDTO(MUNICIPALITY_ID);
		when(errandRepositoryMock.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var note = errandService.addNoteToErrand(errand.getId(), MUNICIPALITY_ID, newNote);

		// Assert
		assertThat(note).isEqualTo(newNote);
		assertThat(errand.getNotes()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addStatusToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newStatus = createStatusDTO();
		when(errandRepositoryMock.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.addStatusToErrand(errand.getId(), MUNICIPALITY_ID, newStatus);

		// Assert
		assertThat(errand.getStatuses()).isNotEmpty().hasSize(2);
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);

		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addDecisionToErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var newDecision = createDecisionDTO(MUNICIPALITY_ID);
		when(errandRepositoryMock.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var decisionDTO = errandService.addDecisionToErrand(errand.getId(), MUNICIPALITY_ID, newDecision);

		// Assert
		assertThat(decisionDTO).isEqualTo(newDecision);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void addAppealToErrandTest() {

		// Arrange
		final var errand = createErrand();
		errand.getDecisions().add(Decision.builder().withId(123L).build());
		final var newAppeal = createAppealDTO(MUNICIPALITY_ID);
		when(errandRepositoryMock.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		final var appealDTO = errandService.addAppealToErrand(errand.getId(), MUNICIPALITY_ID, newAppeal);

		// Assert
		assertThat(appealDTO).isEqualTo(newAppeal);
		assertThat(errand.getDecisions()).isNotEmpty().hasSize(2);

		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
	}

	@Test
	void updateErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var patch = createPatchErrandDto(MUNICIPALITY_ID);
		when(errandRepositoryMock.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(errand));

		// Act
		errandService.updateErrand(errand.getId(), MUNICIPALITY_ID, patch);

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

		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void deleteStakeholderOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());
		// Set ID on every stakeholder
		errand.getStakeholders().forEach(s -> s.setId(new Random().nextLong(1, 1000)));

		final var errandId = new Random().nextLong(1, 1000);
		when(errandRepositoryMock.findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(errand));

		final var stakeholder = errand.getStakeholders().getFirst();

		// Act
		errandService.deleteStakeholderOnErrand(errandId, MUNICIPALITY_ID, stakeholder.getId());

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID);
		verify(processServiceMock).updateProcess(errand);
		verify(errandRepositoryMock).save(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void replaceStatusesOnErrandTest() {

		// Arrange
		final var errand = createErrand();
		final var statuses = List.of(createStatusDTO(), createStatusDTO(), createStatusDTO());
		when(errandRepositoryMock.findByIdAndMunicipalityId(any(Long.class), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.replaceStatusesOnErrand(123L, MUNICIPALITY_ID, statuses);

		// Assert
		assertThat(errand.getStatuses()).isNotEmpty().hasSize(3).allSatisfy(status -> {
			assertThat(status.getDateTime()).isInstanceOf(OffsetDateTime.class).isNotNull();
			assertThat(status.getStatusType()).isInstanceOf(String.class).isNotBlank();
			assertThat(status.getDescription()).isInstanceOf(String.class).isNotBlank();
		});

		verify(errandRepositoryMock).findByIdAndMunicipalityId(123L, MUNICIPALITY_ID);
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

		final var facilityDTO_1 = createFacilityDTO(MUNICIPALITY_ID);
		facilityDTO_1.setId(facilityId_1);
		final var facilityDTO_2 = createFacilityDTO(MUNICIPALITY_ID);
		facilityDTO_2.setId(facilityId_2);

		final var facilities = List.of(facilityDTO_1, facilityDTO_2, createFacilityDTO(MUNICIPALITY_ID));

		when(errandRepositoryMock.findByIdAndMunicipalityId(any(Long.class), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(errand));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.replaceFacilitiesOnErrand(errandId, MUNICIPALITY_ID, facilities);

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

		verify(errandRepositoryMock).findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(any());
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void replaceStakeholderOnErrandTest() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityId();
		final var stakeholders = List.of(createStakeholderDTO(getRandomStakeholderType(), List.of(getRandomStakeholderRole()), MUNICIPALITY_ID));
		when(errandRepositoryMock.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		errandService.replaceStakeholdersOnErrand(errand.getId(), MUNICIPALITY_ID, stakeholders);

		// Assert
		assertThat(errand.getStakeholders()).isEqualTo(stakeholders.stream().map(stakeholderDTO -> toStakeholder(stakeholderDTO, MUNICIPALITY_ID)).toList());

		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	@Test
	void findAllWithoutDuplicates() {

		// Arrange
		final var errandDTO = createErrandDTO(MUNICIPALITY_ID);
		final var returnErrands = Stream.of(errandDTO, errandDTO, errandDTO, errandDTO, errandDTO)
			.map(dto -> EntityMapper.toErrand(dto, MUNICIPALITY_ID))
			.toList();

		when(errandRepositoryMock.findAll(ArgumentMatchers.<Specification<Errand>>any())).thenReturn(returnErrands);
		when(errandRepositoryMock.findAllByIdInAndMunicipalityId(anyList(), eq(MUNICIPALITY_ID), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(returnErrands.getFirst())));

		final Specification<Errand> filterSpecification = filterSpecificationConverterSpy.convert("stakeholders.firstName '*kim*' or stakeholders.lastName ~ '*kim*' or stakeholders.contactInformation.value ~ '*kim*'");
		final Pageable pageable = PageRequest.of(0, 20);

		// Act
		errandService.findAll(filterSpecification, MUNICIPALITY_ID, new HashMap<>(), pageable);

		// Assert
		verify(errandRepositoryMock).findAllByIdInAndMunicipalityId(idListCapture.capture(), eq(MUNICIPALITY_ID), any(Pageable.class));

		assertThat(idListCapture.getValue()).hasSize(1);
		assertThat(idListCapture.getValue().getFirst()).isEqualTo(errandDTO.getId());
	}

	@Test
	void getDecisionsOnErrand() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityId();

		// Act
		final var result = errandService.findDecisionsOnErrand(errand.getId(), MUNICIPALITY_ID);

		// Assert
		assertThat(result).isEqualTo(errand.getDecisions().stream().map(EntityMapper::toDecisionDto).toList());
	}

	@Test
	void getDecisionsOnErrandNotFound() {

		// Arrange
		final var errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		errand.setId(new Random().nextLong(1, 1000));
		errand.setDecisions(new ArrayList<>());
		when(errandRepositoryMock.findByIdAndMunicipalityId(any(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(errand));

		final var id = errand.getId();

		// Act/Assert
		assertThrows(ThrowableProblem.class, () -> errandService.findDecisionsOnErrand(id, MUNICIPALITY_ID));
	}

	@Test
	void deleteDecisionOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		errand.setCaseType(PARKING_PERMIT_RENEWAL.name());

		// Set ID on every decision
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var decision = errand.getDecisions().getFirst();

		when(errandRepositoryMock.findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteDecisionOnErrand(errandId, MUNICIPALITY_ID, decision.getId());

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void deleteNoteOnErrand() {
		// Arrange
		final var errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		errand.setCaseType(ANMALAN_ATTEFALL.name());
		// Set ID on every note
		errand.getNotes().forEach(note -> note.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var note = errand.getNotes().getFirst();

		when(errandRepositoryMock.findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteNoteOnErrand(errandId, MUNICIPALITY_ID, note.getId());

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void deleteAppealOnErrand() {

		// Arrange
		final var errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		// Set ID on every decision
		errand.getDecisions().forEach(d -> d.setId(new Random().nextLong()));

		final var errandId = new Random().nextLong(1, 1000);
		final var appeal = errand.getAppeals().getFirst();
		appeal.setId(new Random().nextLong());

		when(errandRepositoryMock.findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteAppealOnErrand(errandId, MUNICIPALITY_ID, appeal.getId());

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(errand);
		verify(processServiceMock).updateProcess(errand);
	}

	private Errand mockErrandFindByIdAndMunicipalityId() {
		final var errand = toErrand(createErrandDTO(MUNICIPALITY_ID), MUNICIPALITY_ID);
		errand.setId(new Random().nextLong(1, 1000));
		when(errandRepositoryMock.findByIdAndMunicipalityId(any(), eq(MUNICIPALITY_ID))).thenReturn(Optional.of(errand));
		return errand;
	}

	@Test
	void testPatch() {

		// Arrange
		final var dto = new PatchErrandDTO();
		final var entity = new Errand();
		entity.setCaseType(PARKING_PERMIT_RENEWAL.name());
		when(errandRepositoryMock.findByIdAndMunicipalityId(1L, MUNICIPALITY_ID)).thenReturn(Optional.of(entity));
		when(errandRepositoryMock.save(entity)).thenReturn(entity);

		// Act
		errandService.updateErrand(1L, MUNICIPALITY_ID, dto);

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityId(1L, MUNICIPALITY_ID);
		verify(errandRepositoryMock).save(entity);
		verify(processServiceMock).updateProcess(entity);
		verifyNoMoreInteractions(errandRepositoryMock, processServiceMock);
	}

	@Test
	void findFacilitiesOnErrand() {

		// Arrange
		final var errand = mockErrandFindByIdAndMunicipalityId();

		// Act
		final var result = errandService.findFacilitiesOnErrand(errand.getId(), MUNICIPALITY_ID);

		// Assert
		assertThat(errand.getFacilities().stream().map(EntityMapper::toFacilityDto).toList()).isEqualTo(result);
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
		verifyNoMoreInteractions(errandRepositoryMock, facilityRepositoryMock);
	}

	@Test
	void findFacilityOnErrand() {

		// Arrange
		final var errandId = 123L;
		final var facilityId = 456L;
		final var facility = createFacility();
		when(facilityRepositoryMock.findByIdAndErrandIdAndMunicipalityId(facilityId, errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(facility));

		// Act
		final var result = errandService.findFacilityOnErrand(errandId, facilityId, MUNICIPALITY_ID);

		// Assert
		assertThat(result).isEqualTo(toFacilityDto(facility));
		verify(facilityRepositoryMock).findByIdAndErrandIdAndMunicipalityId(facilityId, errandId, MUNICIPALITY_ID);
		verifyNoMoreInteractions(errandRepositoryMock, facilityRepositoryMock);
	}

	@Test
	void createFacilityTest() {

		// Arrange
		final var errand = createErrand();
		final var errandId = errand.getId();
		final var facilityDto = createFacilityDTO(MUNICIPALITY_ID);
		final var facility = toFacility(facilityDto, MUNICIPALITY_ID);

		when(errandRepositoryMock.findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(errand));
		when(facilityRepositoryMock.save(any())).thenReturn(facility);

		// Act
		final var result = errandService.createFacility(errandId, MUNICIPALITY_ID, facilityDto);

		// Assert
		assertThat(result).isEqualTo(facilityDto);
		verify(processServiceMock).updateProcess(errand);
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID);
		verify(facilityRepositoryMock).save(any());
		verifyNoMoreInteractions(processServiceMock, errandRepositoryMock);
	}

	@Test
	void deleteFacilityOnErrand() {

		// Arrange
		final var errand = createErrand();
		final var errandId = errand.getId();
		final var facility = createFacility();
		final var facilityId = facility.getId();

		when(errandRepositoryMock.findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(errand));

		// Act
		errandService.deleteFacilityOnErrand(errandId, MUNICIPALITY_ID, facilityId);

		// Assert
		verify(errandRepositoryMock).findByIdAndMunicipalityId(errandId, MUNICIPALITY_ID);
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
		final var patch = createFacilityDTO(MUNICIPALITY_ID);

		when(errandRepositoryMock.findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID)).thenReturn(Optional.of(errand));
		when(facilityRepositoryMock.findByIdAndErrandIdAndMunicipalityId(facilityId, errandId, MUNICIPALITY_ID)).thenReturn(Optional.of(facility));
		when(facilityRepositoryMock.save(facility)).thenReturn(facility);

		// Act
		final var result = errandService.updateFacilityOnErrand(errandId, MUNICIPALITY_ID, facilityId, patch);

		// Assert
		assertThat(result).isNotNull().satisfies(f -> {
			assertThat(f.getAddress()).isEqualTo(patch.getAddress());
			assertThat(f.getDescription()).isEqualTo(patch.getDescription());
			assertThat(f.getFacilityCollectionName()).isEqualTo(patch.getFacilityCollectionName());
			assertThat(f.getFacilityType()).isEqualTo(patch.getFacilityType());
			assertThat(f.isMainFacility()).isEqualTo(patch.isMainFacility());
			assertThat(f.getExtraParameters()).containsAllEntriesOf(patch.getExtraParameters());
		});

		verify(errandRepositoryMock).findByIdAndMunicipalityId(errand.getId(), MUNICIPALITY_ID);
		verify(facilityRepositoryMock).findByIdAndErrandIdAndMunicipalityId(facilityId, errandId, MUNICIPALITY_ID);
		verify(facilityRepositoryMock).save(facility);
		verify(processServiceMock).updateProcess(errand);
	}
}
